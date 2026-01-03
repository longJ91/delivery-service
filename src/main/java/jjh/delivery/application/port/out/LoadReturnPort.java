package jjh.delivery.application.port.out;

import jjh.delivery.domain.returns.ProductReturn;
import jjh.delivery.domain.returns.ReturnStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Return Load Port - Driven Port (Outbound)
 */
public interface LoadReturnPort {

    Optional<ProductReturn> findById(UUID returnId);

    List<ProductReturn> findByOrderId(UUID orderId);

    List<ProductReturn> findByCustomerId(UUID customerId);

    List<ProductReturn> findByStatus(ReturnStatus status);

    boolean existsById(UUID returnId);
}
