package jjh.delivery.application.port.out;

import jjh.delivery.domain.returns.ProductReturn;
import jjh.delivery.domain.returns.ReturnStatus;

import java.util.List;
import java.util.Optional;

/**
 * Return Load Port - Driven Port (Outbound)
 */
public interface LoadReturnPort {

    Optional<ProductReturn> findById(String returnId);

    List<ProductReturn> findByOrderId(String orderId);

    List<ProductReturn> findByCustomerId(String customerId);

    List<ProductReturn> findByStatus(ReturnStatus status);

    boolean existsById(String returnId);
}
