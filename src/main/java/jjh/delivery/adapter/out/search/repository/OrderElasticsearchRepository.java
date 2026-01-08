package jjh.delivery.adapter.out.search.repository;

import jjh.delivery.adapter.out.search.document.OrderDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

/**
 * Order Elasticsearch Repository
 *
 * Note: ID와 외래키 필드는 UUID 직렬화 문제를 피하기 위해 String으로 처리합니다.
 */
public interface OrderElasticsearchRepository extends ElasticsearchRepository<OrderDocument, String> {

    List<OrderDocument> findByCustomerId(String customerId);

    List<OrderDocument> findBySellerId(String sellerId);

    List<OrderDocument> findByStatus(String status);

    List<OrderDocument> findByCustomerIdAndStatus(String customerId, String status);

    List<OrderDocument> findBySellerIdAndStatus(String sellerId, String status);
}
