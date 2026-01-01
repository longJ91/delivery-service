package jjh.delivery.adapter.out.persistence.jpa.mapper;

import jjh.delivery.adapter.out.persistence.jpa.entity.ProductJpaEntity;
import jjh.delivery.adapter.out.persistence.jpa.entity.ProductVariantJpaEntity;
import jjh.delivery.domain.product.Product;
import jjh.delivery.domain.product.ProductVariant;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Product 영속성 매퍼
 */
@Component
public class ProductPersistenceMapper {

    public Product toDomain(ProductJpaEntity entity) {
        return Product.builder()
                .id(entity.getId())
                .sellerId(entity.getSellerId())
                .name(entity.getName())
                .description(entity.getDescription())
                .basePrice(entity.getBasePrice())
                .status(entity.getStatus())
                .variants(entity.getVariants().stream()
                        .map(this::toDomainVariant)
                        .collect(Collectors.toList()))
                .categoryIds(entity.getCategoryIds())
                .imageUrls(entity.getImageUrls())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private ProductVariant toDomainVariant(ProductVariantJpaEntity entity) {
        return new ProductVariant(
                entity.getId(),
                entity.getName(),
                entity.getSku(),
                entity.getOptionValues(),
                entity.getAdditionalPrice(),
                entity.getStockQuantity(),
                entity.isActive()
        );
    }

    public ProductJpaEntity toEntity(Product domain) {
        ProductJpaEntity entity = new ProductJpaEntity(
                domain.getId(),
                domain.getSellerId(),
                domain.getName(),
                domain.getDescription(),
                domain.getBasePrice(),
                domain.getStatus(),
                domain.getTotalStockQuantity(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );

        entity.setCategoryIds(domain.getCategoryIds());
        entity.setImageUrls(domain.getImageUrls());

        for (ProductVariant variant : domain.getVariants()) {
            entity.addVariant(toEntityVariant(variant));
        }

        return entity;
    }

    private ProductVariantJpaEntity toEntityVariant(ProductVariant domain) {
        return new ProductVariantJpaEntity(
                domain.id(),
                domain.name(),
                domain.sku(),
                domain.optionValues(),
                domain.additionalPrice(),
                domain.stockQuantity(),
                domain.isActive()
        );
    }
}
