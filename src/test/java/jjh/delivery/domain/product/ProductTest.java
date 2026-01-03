package jjh.delivery.domain.product;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Product Aggregate Root Unit Tests
 */
@DisplayName("Product 도메인 테스트")
class ProductTest {

    // Deterministic UUIDs for testing
    private static final UUID SELLER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID CATEGORY_ID_1 = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID CATEGORY_ID_2 = UUID.fromString("00000000-0000-0000-0000-000000000003");

    // =====================================================
    // Test Fixtures
    // =====================================================

    private Product.Builder createValidProductBuilder() {
        return Product.builder()
                .sellerId(SELLER_ID)
                .name("테스트 상품")
                .description("테스트 상품 설명")
                .basePrice(new BigDecimal("50000"));
    }

    private ProductVariant createVariant(String name, int stock) {
        return ProductVariant.of(
                name,
                "SKU-" + name.toUpperCase(),
                Map.of("색상", name),
                BigDecimal.ZERO,
                stock
        );
    }

    // =====================================================
    // 상품 생성 테스트
    // =====================================================

    @Nested
    @DisplayName("상품 생성")
    class ProductCreation {

        @Test
        @DisplayName("필수 필드로 상품 생성 성공")
        void createProductWithRequiredFields() {
            // given & when
            Product product = createValidProductBuilder().build();

            // then
            assertThat(product.getId()).isNotNull();
            assertThat(product.getSellerId()).isEqualTo(SELLER_ID);
            assertThat(product.getName()).isEqualTo("테스트 상품");
            assertThat(product.getBasePrice()).isEqualByComparingTo(new BigDecimal("50000"));
            assertThat(product.getStatus()).isEqualTo(ProductStatus.DRAFT);
            assertThat(product.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("sellerId 없이 생성 시 예외 발생")
        void createWithoutSellerIdThrowsException() {
            assertThatThrownBy(() ->
                    Product.builder()
                            .name("상품")
                            .basePrice(new BigDecimal("10000"))
                            .build()
            ).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("sellerId");
        }

        @Test
        @DisplayName("name 없이 생성 시 예외 발생")
        void createWithoutNameThrowsException() {
            assertThatThrownBy(() ->
                    Product.builder()
                            .sellerId(SELLER_ID)
                            .basePrice(new BigDecimal("10000"))
                            .build()
            ).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("name");
        }

        @Test
        @DisplayName("basePrice 없이 생성 시 예외 발생")
        void createWithoutBasePriceThrowsException() {
            assertThatThrownBy(() ->
                    Product.builder()
                            .sellerId(SELLER_ID)
                            .name("상품")
                            .build()
            ).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("basePrice");
        }

        @Test
        @DisplayName("음수 basePrice로 생성 시 예외 발생")
        void createWithNegativeBasePriceThrowsException() {
            assertThatThrownBy(() ->
                    Product.builder()
                            .sellerId(SELLER_ID)
                            .name("상품")
                            .basePrice(new BigDecimal("-100"))
                            .build()
            ).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("basePrice");
        }

        @Test
        @DisplayName("변형 상품과 함께 생성")
        void createWithVariants() {
            // given
            ProductVariant variant = createVariant("빨강", 10);

            // when
            Product product = createValidProductBuilder()
                    .addVariant(variant)
                    .build();

            // then
            assertThat(product.getVariants()).hasSize(1);
            assertThat(product.hasVariants()).isTrue();
        }
    }

    // =====================================================
    // 상품 정보 업데이트 테스트
    // =====================================================

    @Nested
    @DisplayName("상품 정보 업데이트")
    class ProductUpdate {

        @Test
        @DisplayName("상품 정보 업데이트 성공")
        void updateInfo() {
            // given
            Product product = createValidProductBuilder().build();

            // when
            product.updateInfo("새 상품명", "새 설명", new BigDecimal("60000"));

            // then
            assertThat(product.getName()).isEqualTo("새 상품명");
            assertThat(product.getDescription()).isEqualTo("새 설명");
            assertThat(product.getBasePrice()).isEqualByComparingTo(new BigDecimal("60000"));
        }

        @Test
        @DisplayName("DELETED 상태에서 업데이트 불가")
        void cannotUpdateWhenDeleted() {
            // given
            Product product = createValidProductBuilder()
                    .status(ProductStatus.DELETED)
                    .build();

            // when & then
            assertThatThrownBy(() ->
                    product.updateInfo("새 상품명", "새 설명", new BigDecimal("60000"))
            ).isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not editable");
        }
    }

    // =====================================================
    // 상품 상태 관리 테스트
    // =====================================================

    @Nested
    @DisplayName("상품 상태 관리")
    class ProductStatusManagement {

        @Test
        @DisplayName("재고 있는 상품 활성화 성공")
        void activateProductWithStock() {
            // given
            Product product = createValidProductBuilder()
                    .addVariant(createVariant("빨강", 10))
                    .build();

            // when
            product.activate();

            // then
            assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
        }

        @Test
        @DisplayName("재고 없는 상품 활성화 시 예외")
        void cannotActivateWithoutStock() {
            // given
            Product product = createValidProductBuilder().build();

            // when & then
            assertThatThrownBy(product::activate)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("without stock");
        }

        @Test
        @DisplayName("DELETED 상태에서 활성화 불가")
        void cannotActivateDeletedProduct() {
            // given
            Product product = createValidProductBuilder()
                    .status(ProductStatus.DELETED)
                    .build();

            // when & then
            assertThatThrownBy(product::activate)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("deleted");
        }

        @Test
        @DisplayName("상품 비활성화")
        void deactivateProduct() {
            // given
            Product product = createValidProductBuilder()
                    .addVariant(createVariant("빨강", 10))
                    .status(ProductStatus.ACTIVE)
                    .build();

            // when
            product.deactivate();

            // then
            assertThat(product.getStatus()).isEqualTo(ProductStatus.INACTIVE);
        }

        @Test
        @DisplayName("상품 삭제")
        void deleteProduct() {
            // given
            Product product = createValidProductBuilder().build();

            // when
            product.delete();

            // then
            assertThat(product.getStatus()).isEqualTo(ProductStatus.DELETED);
        }
    }

    // =====================================================
    // 변형 상품 관리 테스트
    // =====================================================

    @Nested
    @DisplayName("변형 상품 관리")
    class VariantManagement {

        @Test
        @DisplayName("변형 상품 추가")
        void addVariant() {
            // given
            Product product = createValidProductBuilder().build();
            ProductVariant variant = createVariant("빨강", 10);

            // when
            product.addVariant(variant);

            // then
            assertThat(product.getVariants()).hasSize(1);
            assertThat(product.hasVariants()).isTrue();
        }

        @Test
        @DisplayName("변형 상품 제거")
        void removeVariant() {
            // given
            ProductVariant variant = createVariant("빨강", 10);
            Product product = createValidProductBuilder()
                    .addVariant(variant)
                    .build();
            UUID variantId = product.getVariants().get(0).id();

            // when
            product.removeVariant(variantId);

            // then
            assertThat(product.getVariants()).isEmpty();
            assertThat(product.hasVariants()).isFalse();
        }

        @Test
        @DisplayName("변형 상품 조회")
        void findVariant() {
            // given
            ProductVariant variant = createVariant("빨강", 10);
            Product product = createValidProductBuilder()
                    .addVariant(variant)
                    .build();
            UUID variantId = product.getVariants().get(0).id();

            // when
            var foundVariant = product.findVariant(variantId);

            // then
            assertThat(foundVariant).isPresent();
            assertThat(foundVariant.get().name()).isEqualTo("빨강");
        }

        @Test
        @DisplayName("존재하지 않는 변형 상품 조회 시 빈 Optional")
        void findNonExistentVariant() {
            // given
            Product product = createValidProductBuilder().build();
            UUID nonExistentId = UUID.fromString("00000000-0000-0000-0000-000000000999");

            // when
            var foundVariant = product.findVariant(nonExistentId);

            // then
            assertThat(foundVariant).isEmpty();
        }
    }

    // =====================================================
    // 재고 관리 테스트
    // =====================================================

    @Nested
    @DisplayName("재고 관리")
    class StockManagement {

        @Test
        @DisplayName("변형 상품 재고 차감")
        void decreaseVariantStock() {
            // given
            ProductVariant variant = createVariant("빨강", 10);
            Product product = createValidProductBuilder()
                    .addVariant(variant)
                    .build();
            UUID variantId = product.getVariants().get(0).id();

            // when
            product.decreaseStock(variantId, 3);

            // then
            assertThat(product.findVariant(variantId).get().stockQuantity()).isEqualTo(7);
        }

        @Test
        @DisplayName("변형 상품 재고 추가")
        void increaseVariantStock() {
            // given
            ProductVariant variant = createVariant("빨강", 10);
            Product product = createValidProductBuilder()
                    .addVariant(variant)
                    .build();
            UUID variantId = product.getVariants().get(0).id();

            // when
            product.increaseStock(variantId, 5);

            // then
            assertThat(product.findVariant(variantId).get().stockQuantity()).isEqualTo(15);
        }

        @Test
        @DisplayName("ACTIVE 상태에서 재고 0이 되면 OUT_OF_STOCK으로 변경")
        void statusChangesToOutOfStockWhenNoStock() {
            // given
            ProductVariant variant = createVariant("빨강", 5);
            Product product = createValidProductBuilder()
                    .addVariant(variant)
                    .status(ProductStatus.ACTIVE)
                    .build();
            UUID variantId = product.getVariants().get(0).id();

            // when
            product.decreaseStock(variantId, 5);

            // then
            assertThat(product.getStatus()).isEqualTo(ProductStatus.OUT_OF_STOCK);
        }

        @Test
        @DisplayName("OUT_OF_STOCK 상태에서 재고 추가 시 ACTIVE로 변경")
        void statusChangesToActiveWhenStockAdded() {
            // given
            ProductVariant variant = ProductVariant.of("빨강", "SKU-RED", Map.of(), BigDecimal.ZERO, 0);
            Product product = createValidProductBuilder()
                    .addVariant(variant)
                    .status(ProductStatus.OUT_OF_STOCK)
                    .build();
            UUID variantId = product.getVariants().get(0).id();

            // when
            product.increaseStock(variantId, 10);

            // then
            assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
        }

        @Test
        @DisplayName("재고 부족 시 예외 발생")
        void throwsExceptionWhenNotEnoughStock() {
            // given
            ProductVariant variant = createVariant("빨강", 5);
            Product product = createValidProductBuilder()
                    .addVariant(variant)
                    .build();
            UUID variantId = product.getVariants().get(0).id();

            // when & then
            assertThatThrownBy(() -> product.decreaseStock(variantId, 10))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Not enough stock");
        }
    }

    // =====================================================
    // 카테고리 관리 테스트
    // =====================================================

    @Nested
    @DisplayName("카테고리 관리")
    class CategoryManagement {

        @Test
        @DisplayName("카테고리 추가")
        void addCategory() {
            // given
            Product product = createValidProductBuilder().build();

            // when
            product.addCategory(CATEGORY_ID_1);
            product.addCategory(CATEGORY_ID_2);

            // then
            assertThat(product.getCategoryIds()).containsExactly(CATEGORY_ID_1, CATEGORY_ID_2);
        }

        @Test
        @DisplayName("중복 카테고리 추가 시 무시")
        void addDuplicateCategoryIgnored() {
            // given
            Product product = createValidProductBuilder().build();

            // when
            product.addCategory(CATEGORY_ID_1);
            product.addCategory(CATEGORY_ID_1);

            // then
            assertThat(product.getCategoryIds()).hasSize(1);
        }

        @Test
        @DisplayName("카테고리 제거")
        void removeCategory() {
            // given
            Product product = createValidProductBuilder()
                    .categoryIds(List.of(CATEGORY_ID_1, CATEGORY_ID_2))
                    .build();

            // when
            product.removeCategory(CATEGORY_ID_1);

            // then
            assertThat(product.getCategoryIds()).containsExactly(CATEGORY_ID_2);
        }
    }

    // =====================================================
    // 이미지 관리 테스트
    // =====================================================

    @Nested
    @DisplayName("이미지 관리")
    class ImageManagement {

        @Test
        @DisplayName("이미지 추가")
        void addImage() {
            // given
            Product product = createValidProductBuilder().build();

            // when
            product.addImage("http://example.com/image1.jpg");
            product.addImage("http://example.com/image2.jpg");

            // then
            assertThat(product.getImageUrls()).hasSize(2);
        }

        @Test
        @DisplayName("이미지 제거")
        void removeImage() {
            // given
            Product product = createValidProductBuilder()
                    .imageUrls(List.of("http://example.com/image1.jpg", "http://example.com/image2.jpg"))
                    .build();

            // when
            product.removeImage("http://example.com/image1.jpg");

            // then
            assertThat(product.getImageUrls()).containsExactly("http://example.com/image2.jpg");
        }
    }

    // =====================================================
    // 스펙 관리 테스트
    // =====================================================

    @Nested
    @DisplayName("스펙 관리")
    class SpecificationManagement {

        @Test
        @DisplayName("스펙 추가")
        void setSpecification() {
            // given
            Product product = createValidProductBuilder().build();

            // when
            product.setSpecification("무게", "500g");
            product.setSpecification("크기", "30x20x10cm");

            // then
            assertThat(product.getSpecifications())
                    .containsEntry("무게", "500g")
                    .containsEntry("크기", "30x20x10cm");
        }

        @Test
        @DisplayName("스펙 수정")
        void updateSpecification() {
            // given
            Product product = createValidProductBuilder()
                    .specifications(Map.of("무게", "500g"))
                    .build();

            // when
            product.setSpecification("무게", "600g");

            // then
            assertThat(product.getSpecifications()).containsEntry("무게", "600g");
        }
    }

    // =====================================================
    // 조회 메서드 테스트
    // =====================================================

    @Nested
    @DisplayName("조회 메서드")
    class QueryMethods {

        @Test
        @DisplayName("판매 가능 여부 확인 - 활성 상태 + 재고 있음")
        void isSellableWhenActiveAndHasStock() {
            // given
            Product product = createValidProductBuilder()
                    .addVariant(createVariant("빨강", 10))
                    .status(ProductStatus.ACTIVE)
                    .build();

            // then
            assertThat(product.isSellable()).isTrue();
        }

        @Test
        @DisplayName("판매 불가 - INACTIVE 상태")
        void isNotSellableWhenInactive() {
            // given
            Product product = createValidProductBuilder()
                    .addVariant(createVariant("빨강", 10))
                    .status(ProductStatus.INACTIVE)
                    .build();

            // then
            assertThat(product.isSellable()).isFalse();
        }

        @Test
        @DisplayName("최종 가격 계산 - 기본 가격만")
        void calculatePriceWithoutVariant() {
            // given
            Product product = createValidProductBuilder().build();

            // when
            BigDecimal price = product.calculatePrice(null);

            // then
            assertThat(price).isEqualByComparingTo(new BigDecimal("50000"));
        }

        @Test
        @DisplayName("최종 가격 계산 - 변형 상품 추가 가격 포함")
        void calculatePriceWithVariant() {
            // given
            ProductVariant variant = ProductVariant.of(
                    "프리미엄",
                    "SKU-PREMIUM",
                    Map.of(),
                    new BigDecimal("10000"),  // 추가 가격
                    10
            );
            Product product = createValidProductBuilder()
                    .addVariant(variant)
                    .build();
            UUID variantId = product.getVariants().get(0).id();

            // when
            BigDecimal price = product.calculatePrice(variantId);

            // then (50,000 + 10,000 = 60,000)
            assertThat(price).isEqualByComparingTo(new BigDecimal("60000"));
        }

        @Test
        @DisplayName("총 재고 수량 계산")
        void getTotalStockQuantity() {
            // given
            Product product = createValidProductBuilder()
                    .addVariant(createVariant("빨강", 10))
                    .addVariant(createVariant("파랑", 20))
                    .build();

            // then
            assertThat(product.getTotalStockQuantity()).isEqualTo(30);
        }

        @Test
        @DisplayName("컬렉션 반환값 불변")
        void collectionsAreImmutable() {
            // given
            Product product = createValidProductBuilder()
                    .addVariant(createVariant("빨강", 10))
                    .categoryIds(List.of(CATEGORY_ID_1))
                    .imageUrls(List.of("img-1"))
                    .specifications(Map.of("key", "value"))
                    .build();

            // when & then
            assertThatThrownBy(() -> product.getVariants().clear())
                    .isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> product.getCategoryIds().clear())
                    .isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> product.getImageUrls().clear())
                    .isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> product.getSpecifications().clear())
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }
}
