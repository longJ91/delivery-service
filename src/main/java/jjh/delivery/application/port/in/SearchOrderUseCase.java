package jjh.delivery.application.port.in;

import jjh.delivery.domain.order.Order;
import jjh.delivery.domain.order.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Search Order Use Case - Driving Port (Inbound)
 * Elasticsearch를 활용한 검색 기능
 */
public interface SearchOrderUseCase {

    List<Order> searchOrders(SearchOrderQuery query);

    List<Order> findByCustomerId(String customerId);

    List<Order> findBySellerId(String sellerId);

    record SearchOrderQuery(
            String customerId,
            String sellerId,
            OrderStatus status,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            String keyword,
            int page,
            int size
    ) {
        public SearchOrderQuery {
            if (page < 0) page = 0;
            if (size <= 0) size = 20;
            if (size > 100) size = 100;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String customerId;
            private String sellerId;
            private OrderStatus status;
            private LocalDateTime fromDate;
            private LocalDateTime toDate;
            private String keyword;
            private int page = 0;
            private int size = 20;

            public Builder customerId(String customerId) {
                this.customerId = customerId;
                return this;
            }

            public Builder sellerId(String sellerId) {
                this.sellerId = sellerId;
                return this;
            }

            public Builder status(OrderStatus status) {
                this.status = status;
                return this;
            }

            public Builder fromDate(LocalDateTime fromDate) {
                this.fromDate = fromDate;
                return this;
            }

            public Builder toDate(LocalDateTime toDate) {
                this.toDate = toDate;
                return this;
            }

            public Builder keyword(String keyword) {
                this.keyword = keyword;
                return this;
            }

            public Builder page(int page) {
                this.page = page;
                return this;
            }

            public Builder size(int size) {
                this.size = size;
                return this;
            }

            public SearchOrderQuery build() {
                return new SearchOrderQuery(
                        customerId, sellerId, status, fromDate, toDate, keyword, page, size
                );
            }
        }
    }
}
