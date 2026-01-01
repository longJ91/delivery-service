package jjh.delivery.application.port.in;

import jjh.delivery.domain.order.Order;

import java.math.BigDecimal;
import java.util.List;

/**
 * Create Order Use Case - Driving Port (Inbound)
 */
public interface CreateOrderUseCase {

    int MAX_ORDER_ITEMS = 50;

    Order createOrder(CreateOrderCommand command);

    /**
     * Create Order Command
     * 비즈니스 규칙 (Business Validation) 담당
     * - 형식 검증은 Request DTO에서 처리됨
     */
    record CreateOrderCommand(
            String customerId,
            String shopId,
            List<OrderItemCommand> items,
            String deliveryAddress
    ) {
        public CreateOrderCommand {
            // 비즈니스 규칙만 검증 (형식 검증은 Request DTO에서 이미 완료)
            if (items != null && items.size() > MAX_ORDER_ITEMS) {
                throw new IllegalArgumentException(
                        "한 주문에 " + MAX_ORDER_ITEMS + "개 이상의 항목을 담을 수 없습니다");
            }
        }
    }

    record OrderItemCommand(
            String menuId,
            String menuName,
            int quantity,
            BigDecimal unitPrice
    ) {}
}
