package jjh.delivery.adapter.out.search.repository;

import jjh.delivery.adapter.out.search.document.OrderDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;
import java.util.UUID;

/**
 * Order Elasticsearch Repository
 */
public interface OrderElasticsearchRepository extends ElasticsearchRepository<OrderDocument, UUID> {

    List<OrderDocument> findByCustomerId(UUID customerId);

    List<OrderDocument> findBySellerId(UUID sellerId);

    List<OrderDocument> findByStatus(String status);

    List<OrderDocument> findByCustomerIdAndStatus(UUID customerId, String status);

    List<OrderDocument> findBySellerIdAndStatus(UUID sellerId, String status);
}
