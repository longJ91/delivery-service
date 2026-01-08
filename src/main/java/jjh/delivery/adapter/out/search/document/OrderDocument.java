package jjh.delivery.adapter.out.search.document;

import jjh.delivery.domain.order.Order;
import jjh.delivery.domain.order.OrderItem;
import jjh.delivery.domain.order.OrderStatus;
import jjh.delivery.domain.order.ShippingAddress;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Order Elasticsearch Document (v2 - Product Delivery)
 *
 * Note: UUID 필드는 Elasticsearch에서 올바른 직렬화를 위해 String으로 저장합니다.
 * UUID는 기본적으로 객체로 직렬화되어 쿼리 시 타입 불일치가 발생할 수 있습니다.
 */
@Document(indexName = "orders")
public class OrderDocument {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String orderNumber;

    @Field(type = FieldType.Keyword)
    private String customerId;

    @Field(type = FieldType.Keyword)
    private String sellerId;

    @Field(type = FieldType.Nested)
    private List<OrderItemDocument> items;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Double)
    private BigDecimal totalAmount;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String shippingAddress;

    @Field(type = FieldType.Date)
    private LocalDateTime createdAt;

    @Field(type = FieldType.Date)
    private LocalDateTime updatedAt;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String searchableText;

    public OrderDocument() {
    }

    public static OrderDocument from(Order order) {
        OrderDocument doc = new OrderDocument();
        doc.id = order.getId().toString();
        doc.orderNumber = order.getOrderNumber();
        doc.customerId = order.getCustomerId().toString();
        doc.sellerId = order.getSellerId().toString();
        doc.items = order.getItems().stream()
                .map(OrderItemDocument::from)
                .toList();
        doc.status = order.getStatus().name();
        doc.totalAmount = order.getTotalAmount();
        doc.shippingAddress = order.getShippingAddress() != null
                ? order.getShippingAddress().getFullAddress()
                : null;
        doc.createdAt = order.getCreatedAt();
        doc.updatedAt = order.getUpdatedAt();
        doc.searchableText = buildSearchableText(order);
        return doc;
    }

    private static String buildSearchableText(Order order) {
        StringBuilder sb = new StringBuilder();
        sb.append(order.getOrderNumber()).append(" ");
        if (order.getShippingAddress() != null) {
            sb.append(order.getShippingAddress().getFullAddress()).append(" ");
        }
        order.getItems().forEach(item ->
                sb.append(item.productName()).append(" ")
        );
        return sb.toString().trim();
    }

    public Order toDomain() {
        List<OrderItem> orderItems = items.stream()
                .map(OrderItemDocument::toDomain)
                .toList();

        return Order.builder()
                .id(UUID.fromString(id))
                .orderNumber(orderNumber)
                .customerId(UUID.fromString(customerId))
                .sellerId(UUID.fromString(sellerId))
                .items(orderItems)
                .status(OrderStatus.valueOf(status))
                .shippingAddress(ShippingAddress.of(
                        "Recipient", // placeholder - full info not stored in search index
                        "010-0000-0000",
                        "00000",
                        shippingAddress != null ? shippingAddress : "Unknown"
                ))
                .createdAt(createdAt)
                .build();
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getSellerId() {
        return sellerId;
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

    public String getShippingAddress() {
        return shippingAddress;
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
            String productId,
            String productName,
            String variantId,
            String variantName,
            String sku,
            Map<String, String> optionValues,
            int quantity,
            BigDecimal unitPrice
    ) {
        public static OrderItemDocument from(OrderItem item) {
            return new OrderItemDocument(
                    item.productId().toString(),
                    item.productName(),
                    item.variantId() != null ? item.variantId().toString() : null,
                    item.variantName(),
                    item.sku(),
                    item.optionValues(),
                    item.quantity(),
                    item.unitPrice()
            );
        }

        public OrderItem toDomain() {
            if (variantId != null) {
                return OrderItem.ofVariant(
                        UUID.fromString(productId), productName,
                        UUID.fromString(variantId), variantName,
                        sku, optionValues, quantity, unitPrice
                );
            }
            return OrderItem.of(UUID.fromString(productId), productName, quantity, unitPrice);
        }
    }
}
