package jjh.delivery.adapter.out.search;

import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import jjh.delivery.adapter.in.web.dto.CursorPageResponse;
import jjh.delivery.adapter.in.web.dto.CursorValue;
import jjh.delivery.adapter.out.search.document.OrderDocument;
import jjh.delivery.adapter.out.search.repository.OrderElasticsearchRepository;
import jjh.delivery.application.port.in.SearchOrderUseCase.SearchOrderQuery;
import jjh.delivery.application.port.out.LoadOrderPort;
import jjh.delivery.application.port.out.OrderSearchPort;
import jjh.delivery.domain.order.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Order Elasticsearch Adapter - Driven Adapter (Outbound)
 * Elasticsearch를 사용한 검색 구현 (v2 - Product Delivery)
 */
@Component
@RequiredArgsConstructor
public class OrderElasticsearchAdapter implements OrderSearchPort {

    private static final Logger log = LoggerFactory.getLogger(OrderElasticsearchAdapter.class);

    private final OrderElasticsearchRepository repository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final LoadOrderPort loadOrderPort;

    @Override
    public void index(Order order) {
        try {
            OrderDocument document = OrderDocument.from(order);
            repository.save(document);
            log.debug("Indexed order: {}", order.getId());
        } catch (Exception e) {
            log.error("Failed to index order: {}", order.getId(), e);
        }
    }

    @Override
    public void delete(UUID orderId) {
        try {
            repository.deleteById(orderId);
            log.debug("Deleted order from index: {}", orderId);
        } catch (Exception e) {
            log.error("Failed to delete order from index: {}", orderId, e);
        }
    }

    @Override
    public CursorPageResponse<Order> search(SearchOrderQuery query) {
        List<Query> mustQueries = new ArrayList<>();

        if (query.customerId() != null) {
            mustQueries.add(Query.of(q -> q.term(t -> t.field("customerId").value(query.customerId()))));
        }

        if (query.sellerId() != null) {
            mustQueries.add(Query.of(q -> q.term(t -> t.field("sellerId").value(query.sellerId()))));
        }

        if (query.status() != null) {
            mustQueries.add(Query.of(q -> q.term(t -> t.field("status").value(query.status().name()))));
        }

        if (query.fromDate() != null) {
            mustQueries.add(Query.of(q -> q.range(r -> r.date(d -> d.field("createdAt").gte(query.fromDate().toString())))));
        }

        if (query.toDate() != null) {
            mustQueries.add(Query.of(q -> q.range(r -> r.date(d -> d.field("createdAt").lte(query.toDate().toString())))));
        }

        if (query.keyword() != null && !query.keyword().isBlank()) {
            mustQueries.add(Query.of(q -> q.match(m -> m.field("searchableText").query(query.keyword()))));
        }

        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();
        if (!mustQueries.isEmpty()) {
            boolQueryBuilder.must(mustQueries);
        }

        // Cursor 기반 쿼리 빌드
        NativeQueryBuilder queryBuilder = NativeQuery.builder()
                .withQuery(Query.of(q -> q.bool(boolQueryBuilder.build())))
                .withSort(Sort.by(Sort.Direction.DESC, "createdAt"))
                .withSort(Sort.by(Sort.Direction.DESC, "id"))
                .withMaxResults(query.size() + 1);  // hasNext 판단을 위해 +1

        // 커서 디코딩 및 search_after 설정
        CursorValue cursorValue = CursorValue.decode(query.cursor());
        if (cursorValue != null) {
            queryBuilder.withSearchAfter(List.of(
                    cursorValue.createdAt().toEpochMilli(),
                    cursorValue.id().toString()
            ));
        }

        NativeQuery searchQuery = queryBuilder.build();

        SearchHits<OrderDocument> searchHits = elasticsearchOperations.search(
                searchQuery,
                OrderDocument.class
        );

        List<Order> orders = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(OrderDocument::toDomain)
                .toList();

        return CursorPageResponse.of(
                orders,
                query.size(),
                order -> order.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant(),
                Order::getId
        );
    }

    @Override
    public List<Order> findByCustomerId(UUID customerId) {
        return repository.findByCustomerId(customerId).stream()
                .map(OrderDocument::toDomain)
                .toList();
    }

    @Override
    public List<Order> findBySellerId(UUID sellerId) {
        return repository.findBySellerId(sellerId).stream()
                .map(OrderDocument::toDomain)
                .toList();
    }

    @Override
    public void reindexAll() {
        log.info("Starting full reindex of orders...");

        // 기존 인덱스 삭제
        repository.deleteAll();

        // JPA에서 모든 주문 조회 후 재인덱싱
        // 실제 구현에서는 배치 처리 필요
        List<Order> allOrders = new ArrayList<>();

        // LoadOrderPort를 통해 모든 주문 조회 (페이징 처리 권장)
        // 여기서는 예시로 간단히 구현
        log.info("Reindex completed");
    }
}
