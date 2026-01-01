package jjh.delivery.adapter.out.persistence.jpa.mapper;

import jjh.delivery.adapter.out.persistence.jpa.entity.SellerJpaEntity;
import jjh.delivery.adapter.out.persistence.jpa.entity.WarehouseAddressEmbeddable;
import jjh.delivery.domain.seller.Seller;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 * Seller Persistence Mapper
 * Domain <-> JPA Entity 변환
 */
@Component
public class SellerPersistenceMapper {

    /**
     * JPA Entity → Domain
     */
    public Seller toDomain(SellerJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        Seller.Builder builder = Seller.builder()
                .id(entity.getId())
                .businessName(entity.getBusinessName())
                .businessNumber(entity.getBusinessNumber())
                .representativeName(entity.getRepresentativeName())
                .email(entity.getEmail())
                .phoneNumber(entity.getPhoneNumber())
                .sellerType(entity.getSellerType())
                .status(entity.getStatus())
                .categoryIds(new ArrayList<>(entity.getCategoryIds()))
                .createdAt(entity.getCreatedAt())
                .approvedAt(entity.getApprovedAt());

        if (entity.getWarehouseAddress() != null) {
            builder.warehouseAddress(entity.getWarehouseAddress().toDomain());
        }

        return builder.build();
    }

    /**
     * Domain → JPA Entity
     */
    public SellerJpaEntity toEntity(Seller domain) {
        if (domain == null) {
            return null;
        }

        WarehouseAddressEmbeddable warehouseAddress = null;
        if (domain.getWarehouseAddress() != null) {
            warehouseAddress = WarehouseAddressEmbeddable.fromDomain(domain.getWarehouseAddress());
        }

        return new SellerJpaEntity(
                domain.getId(),
                domain.getBusinessName(),
                domain.getBusinessNumber(),
                domain.getRepresentativeName(),
                domain.getEmail(),
                domain.getPhoneNumber(),
                domain.getSellerType(),
                domain.getStatus(),
                warehouseAddress,
                new ArrayList<>(domain.getCategoryIds()),
                domain.getCreatedAt(),
                domain.getUpdatedAt(),
                domain.getApprovedAt()
        );
    }
}
