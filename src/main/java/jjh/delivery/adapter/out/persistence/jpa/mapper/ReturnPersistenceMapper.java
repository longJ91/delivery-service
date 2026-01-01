package jjh.delivery.adapter.out.persistence.jpa.mapper;

import jjh.delivery.adapter.out.persistence.jpa.entity.ReturnItemJpaEntity;
import jjh.delivery.adapter.out.persistence.jpa.entity.ReturnJpaEntity;
import jjh.delivery.domain.returns.ProductReturn;
import jjh.delivery.domain.returns.ReturnItem;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Return Persistence Mapper
 * Entity <-> Domain 변환
 */
@Component
public class ReturnPersistenceMapper {

    public ProductReturn toDomain(ReturnJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        List<ReturnItem> items = entity.getItems().stream()
                .map(this::toItemDomain)
                .toList();

        return ProductReturn.builder()
                .id(entity.getId())
                .orderId(entity.getOrderId())
                .customerId(entity.getCustomerId())
                .returnType(entity.getReturnType())
                .reason(entity.getReason())
                .reasonDetail(entity.getReasonDetail())
                .status(entity.getStatus())
                .items(items)
                .rejectReason(entity.getRejectReason())
                .createdAt(entity.getCreatedAt())
                .completedAt(entity.getCompletedAt())
                .build();
    }

    public ReturnJpaEntity toEntity(ProductReturn domain) {
        if (domain == null) {
            return null;
        }

        ReturnJpaEntity entity = new ReturnJpaEntity(
                domain.getId(),
                domain.getOrderId(),
                domain.getCustomerId(),
                domain.getReturnType(),
                domain.getReason(),
                domain.getReasonDetail(),
                domain.getStatus(),
                domain.getTotalRefundAmount(),
                domain.getRejectReason(),
                domain.getCreatedAt(),
                domain.getUpdatedAt(),
                domain.getCompletedAt()
        );

        // Add items
        entity.clearItems();
        for (ReturnItem item : domain.getItems()) {
            ReturnItemJpaEntity itemEntity = toItemEntity(item);
            entity.addItem(itemEntity);
        }

        return entity;
    }

    private ReturnItem toItemDomain(ReturnItemJpaEntity entity) {
        return new ReturnItem(
                entity.getId(),
                entity.getOrderItemId(),
                entity.getProductId(),
                entity.getProductName(),
                entity.getVariantId(),
                entity.getVariantName(),
                entity.getQuantity(),
                entity.getRefundAmount()
        );
    }

    private ReturnItemJpaEntity toItemEntity(ReturnItem domain) {
        return new ReturnItemJpaEntity(
                domain.id(),
                domain.orderItemId(),
                domain.productId(),
                domain.productName(),
                domain.variantId(),
                domain.variantName(),
                domain.quantity(),
                domain.refundAmount()
        );
    }
}
