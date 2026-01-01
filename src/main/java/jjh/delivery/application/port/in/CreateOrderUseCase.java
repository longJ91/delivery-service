package jjh.delivery.application.port.in;

import jjh.delivery.domain.order.Order;
import jjh.delivery.domain.order.ShippingAddress;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Create Order Use Case - Driving Port (Inbound)
 * v2 - Product Delivery
 */
public interface CreateOrderUseCase {

    int MAX_ORDER_ITEMS = 50;

    Order createOrder(CreateOrderCommand command);

    /**
     * Create Order Command
     * 비즈니스 규칙 (Business Validation) 담당
     */
    record CreateOrderCommand(
            String customerId,
            String sellerId,
            List<OrderItemCommand> items,
            ShippingAddress shippingAddress,
            String orderMemo,
            String shippingMemo,
            String couponId
    ) {
        public CreateOrderCommand {
            if (items != null && items.size() > MAX_ORDER_ITEMS) {
                throw new IllegalArgumentException(
                        "한 주문에 " + MAX_ORDER_ITEMS + "개 이상의 항목을 담을 수 없습니다");
            }
        }
    }

    record OrderItemCommand(
            String productId,
            String productName,
            String variantId,
            String variantName,
            String sku,
            Map<String, String> optionValues,
            int quantity,
            BigDecimal unitPrice
    ) {
        /**
         * Factory for simple product (no variant)
         */
        public static OrderItemCommand of(String productId, String productName, int quantity, BigDecimal unitPrice) {
            return new OrderItemCommand(productId, productName, null, null, null, null, quantity, unitPrice);
        }

        /**
         * Factory for variant product
         */
        public static OrderItemCommand ofVariant(
                String productId, String productName,
                String variantId, String variantName, String sku,
                Map<String, String> optionValues,
                int quantity, BigDecimal unitPrice
        ) {
            return new OrderItemCommand(productId, productName, variantId, variantName, sku, optionValues, quantity, unitPrice);
        }
    }
}
