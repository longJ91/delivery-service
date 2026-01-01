package jjh.delivery.adapter.out.persistence.jpa.mapper;

import jjh.delivery.adapter.out.persistence.jpa.entity.CouponJpaEntity;
import jjh.delivery.domain.promotion.Coupon;
import org.springframework.stereotype.Component;

/**
 * Coupon Persistence Mapper
 * Domain <-> JPA Entity 변환
 */
@Component
public class CouponPersistenceMapper {

    /**
     * JPA Entity → Domain
     */
    public Coupon toDomain(CouponJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return Coupon.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .description(entity.getDescription())
                .discountType(entity.getDiscountType())
                .discountValue(entity.getDiscountValue())
                .minimumOrderAmount(entity.getMinimumOrderAmount())
                .maximumDiscountAmount(entity.getMaximumDiscountAmount())
                .scope(entity.getScope())
                .scopeTargetId(entity.getScopeTargetId())
                .totalQuantity(entity.getTotalQuantity())
                .usedQuantity(entity.getUsedQuantity())
                .validFrom(entity.getValidFrom())
                .validUntil(entity.getValidUntil())
                .isActive(entity.isActive())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    /**
     * Domain → JPA Entity
     */
    public CouponJpaEntity toEntity(Coupon domain) {
        if (domain == null) {
            return null;
        }

        return new CouponJpaEntity(
                domain.getId(),
                domain.getCode(),
                domain.getName(),
                domain.getDescription(),
                domain.getDiscountType(),
                domain.getDiscountValue(),
                domain.getMinimumOrderAmount(),
                domain.getMaximumDiscountAmount(),
                domain.getScope(),
                domain.getScopeTargetId(),
                domain.getTotalQuantity(),
                domain.getUsedQuantity(),
                domain.getValidFrom(),
                domain.getValidUntil(),
                domain.isActive(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }
}
