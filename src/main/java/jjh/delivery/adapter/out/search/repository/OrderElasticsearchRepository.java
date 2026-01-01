package jjh.delivery.adapter.out.search.repository;

import jjh.delivery.adapter.out.search.document.OrderDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

/**
 * Order Elasticsearch Repository
 */
public interface OrderElasticsearchRepository extends ElasticsearchRepository<OrderDocument, String> {

    List<OrderDocument> findByCustomerId(String customerId);

    List<OrderDocument> findByShopId(String shopId);

    List<OrderDocument> findByStatus(String status);

    List<OrderDocument> findByCustomerIdAndStatus(String customerId, String status);

    List<OrderDocument> findByShopIdAndStatus(String shopId, String status);
}
