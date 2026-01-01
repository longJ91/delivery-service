package jjh.delivery.application.port.out;

import jjh.delivery.domain.returns.ProductReturn;

/**
 * Return Save Port - Driven Port (Outbound)
 */
public interface SaveReturnPort {

    ProductReturn save(ProductReturn productReturn);
}
