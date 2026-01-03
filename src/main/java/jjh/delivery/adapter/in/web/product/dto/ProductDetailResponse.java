package jjh.delivery.adapter.in.web.product.dto;

import jjh.delivery.domain.product.Product;
import jjh.delivery.domain.product.ProductVariant;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 상품 상세 응답
 */
public record ProductDetailResponse(
        String id,
        String sellerId,
        String sellerName,
        String categoryId,
        String name,
        String description,
        BigDecimal price,
        BigDecimal discountPrice,
        int stock,
        String sku,
        String status,
        boolean isFeatured,
        List<ProductImageResponse> images,
        List<ProductVariantResponse> variants,
        double ratingAvg,
        long reviewCount,
        long salesCount,
        LocalDateTime createdAt
) {
    public static ProductDetailResponse from(
            Product product,
            String sellerName,
            double ratingAvg,
            long reviewCount
    ) {
        String primaryCategoryId = product.getCategoryIds().isEmpty() ? null : product.getCategoryIds().get(0).toString();
        String primarySku = product.getVariants().isEmpty() ? null : product.getVariants().get(0).sku();

        List<ProductImageResponse> imageResponses = new java.util.ArrayList<>();
        for (int i = 0; i < product.getImageUrls().size(); i++) {
            imageResponses.add(new ProductImageResponse(
                    "img-" + i,
                    product.getImageUrls().get(i),
                    i == 0
            ));
        }

        return new ProductDetailResponse(
                product.getId().toString(),
                product.getSellerId().toString(),
                sellerName,
                primaryCategoryId,
                product.getName(),
                product.getDescription(),
                product.getBasePrice(),
                null, // discountPrice
                product.getTotalStockQuantity(),
                primarySku,
                product.getStatus().name(),
                false, // isFeatured
                imageResponses,
                product.getVariants().stream()
                        .map(ProductVariantResponse::from)
                        .toList(),
                ratingAvg,
                reviewCount,
                0L, // salesCount
                product.getCreatedAt()
        );
    }

    public record ProductImageResponse(
            String id,
            String imageUrl,
            boolean isPrimary
    ) {}

    public record ProductVariantResponse(
            String id,
            String name,
            String sku,
            BigDecimal price,
            int stock,
            Map<String, String> optionValues
    ) {
        public static ProductVariantResponse from(ProductVariant variant) {
            return new ProductVariantResponse(
                    variant.id().toString(),
                    variant.name(),
                    variant.sku(),
                    variant.additionalPrice(),
                    variant.stockQuantity(),
                    variant.optionValues()
            );
        }
    }
}
