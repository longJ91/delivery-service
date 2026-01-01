package jjh.delivery.adapter.out.search.document;

import jjh.delivery.domain.order.Order;
import jjh.delivery.domain.order.OrderItem;
import jjh.delivery.domain.order.OrderStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Order Elasticsearch Document
 */
@Document(indexName = "orders")
public class OrderDocument {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String customerId;

    @Field(type = FieldType.Keyword)
    private String shopId;

    @Field(type = FieldType.Nested)
    private List<OrderItemDocument> items;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Double)
    private BigDecimal totalAmount;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String deliveryAddress;

    @Field(type = FieldType.Date)
    private LocalDateTime createdAt;

    @Field(type = FieldType.Date)
    private LocalDateTime updatedAt;

    // 검색용 복합 필드
    @Field(type = FieldType.Text, analyzer = "standard")
    private String searchableText;

    public OrderDocument() {
    }

    public static OrderDocument from(Order order) {
        OrderDocument doc = new OrderDocument();
        doc.id = order.getId();
        doc.customerId = order.getCustomerId();
        doc.shopId = order.getShopId();
        doc.items = order.getItems().stream()
                .map(OrderItemDocument::from)
                .toList();
        doc.status = order.getStatus().name();
        doc.totalAmount = order.calculateTotalAmount();
        doc.deliveryAddress = order.getDeliveryAddress();
        doc.createdAt = order.getCreatedAt();
        doc.updatedAt = order.getUpdatedAt();
        doc.searchableText = buildSearchableText(order);
        return doc;
    }

    private static String buildSearchableText(Order order) {
        StringBuilder sb = new StringBuilder();
        sb.append(order.getDeliveryAddress()).append(" ");
        order.getItems().forEach(item ->
                sb.append(item.menuName()).append(" ")
        );
        return sb.toString().trim();
    }

    public Order toDomain() {
        List<OrderItem> orderItems = items.stream()
                .map(OrderItemDocument::toDomain)
                .toList();

        return Order.builder()
                .id(id)
                .customerId(customerId)
                .shopId(shopId)
                .items(orderItems)
                .status(OrderStatus.valueOf(status))
                .deliveryAddress(deliveryAddress)
                .createdAt(createdAt)
                .build();
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getShopId() {
        return shopId;
    }

    public List<OrderItemDocument> getItems() {
        return items;
    }

    public String getStatus() {
        return status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public String getSearchableText() {
        return searchableText;
    }

    public record OrderItemDocument(
            String menuId,
            String menuName,
            int quantity,
            BigDecimal unitPrice
    ) {
        public static OrderItemDocument from(OrderItem item) {
            return new OrderItemDocument(
                    item.menuId(),
                    item.menuName(),
                    item.quantity(),
                    item.unitPrice()
            );
        }

        public OrderItem toDomain() {
            return new OrderItem(menuId, menuName, quantity, unitPrice);
        }
    }
}
