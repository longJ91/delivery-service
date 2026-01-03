package jjh.delivery.domain.product;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * ProductVariant Value Object Unit Tests
 */
@DisplayName("ProductVariant 도메인 테스트")
class ProductVariantTest {

    // Deterministic UUIDs for testing
    private static final UUID VARIANT_ID_1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID VARIANT_ID_2 = UUID.fromString("00000000-0000-0000-0000-000000000002");

    // =====================================================
    // 생성 테스트
    // =====================================================

    @Nested
    @DisplayName("변형 상품 생성")
    class VariantCreation {

        @Test
        @DisplayName("팩토리 메서드로 생성 성공")
        void createWithFactoryMethod() {
            // given & when
            ProductVariant variant = ProductVariant.of(
                    "빨강/L",
                    "SKU-RED-L",
                    Map.of("색상", "빨강", "사이즈", "L"),
                    new BigDecimal("5000"),
                    100
            );

            // then
            assertThat(variant.id()).isNotNull();
            assertThat(variant.name()).isEqualTo("빨강/L");
            assertThat(variant.sku()).isEqualTo("SKU-RED-L");
            assertThat(variant.optionValues()).containsEntry("색상", "빨강");
            assertThat(variant.additionalPrice()).isEqualByComparingTo(new BigDecimal("5000"));
            assertThat(variant.stockQuantity()).isEqualTo(100);
            assertThat(variant.isActive()).isTrue();
        }

        @Test
        @DisplayName("name 없이 생성 시 예외 발생")
        void createWithoutNameThrowsException() {
            assertThatThrownBy(() ->
                    new ProductVariant(VARIANT_ID_1, null, "SKU", Map.of(), BigDecimal.ZERO, 10, true)
            ).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("name");
        }

        @Test
        @DisplayName("빈 name으로 생성 시 예외 발생")
        void createWithBlankNameThrowsException() {
            assertThatThrownBy(() ->
                    new ProductVariant(VARIANT_ID_1, "  ", "SKU", Map.of(), BigDecimal.ZERO, 10, true)
            ).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("name");
        }

        @Test
        @DisplayName("음수 재고로 생성 시 예외 발생")
        void createWithNegativeStockThrowsException() {
            assertThatThrownBy(() ->
                    new ProductVariant(VARIANT_ID_1, "옵션", "SKU", Map.of(), BigDecimal.ZERO, -1, true)
            ).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Stock quantity");
        }

        @Test
        @DisplayName("null additionalPrice는 ZERO로 변환")
        void nullAdditionalPriceBecomesZero() {
            // given & when
            ProductVariant variant = new ProductVariant(
                    VARIANT_ID_1, "옵션", "SKU", Map.of(), null, 10, true
            );

            // then
            assertThat(variant.additionalPrice()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("null optionValues는 빈 맵으로 변환")
        void nullOptionValuesBecomesEmptyMap() {
            // given & when
            ProductVariant variant = new ProductVariant(
                    VARIANT_ID_1, "옵션", "SKU", null, BigDecimal.ZERO, 10, true
            );

            // then
            assertThat(variant.optionValues()).isNotNull();
            assertThat(variant.optionValues()).isEmpty();
        }

        @Test
        @DisplayName("optionValues는 불변")
        void optionValuesIsImmutable() {
            // given
            ProductVariant variant = ProductVariant.of(
                    "옵션", "SKU", Map.of("색상", "빨강"), BigDecimal.ZERO, 10
            );

            // when & then
            assertThatThrownBy(() -> variant.optionValues().put("새키", "새값"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    // =====================================================
    // 재고 확인 테스트
    // =====================================================

    @Nested
    @DisplayName("재고 확인")
    class StockCheck {

        @Test
        @DisplayName("재고 있음")
        void hasStock() {
            // given
            ProductVariant variant = ProductVariant.of("옵션", "SKU", Map.of(), BigDecimal.ZERO, 10);

            // then
            assertThat(variant.hasStock()).isTrue();
        }

        @Test
        @DisplayName("재고 없음")
        void hasNoStock() {
            // given
            ProductVariant variant = ProductVariant.of("옵션", "SKU", Map.of(), BigDecimal.ZERO, 0);

            // then
            assertThat(variant.hasStock()).isFalse();
        }
    }

    // =====================================================
    // 재고 관리 테스트
    // =====================================================

    @Nested
    @DisplayName("재고 관리")
    class StockManagement {

        @Test
        @DisplayName("재고 차감")
        void decreaseStock() {
            // given
            ProductVariant variant = ProductVariant.of("옵션", "SKU", Map.of(), BigDecimal.ZERO, 10);

            // when
            ProductVariant updated = variant.decreaseStock(3);

            // then
            assertThat(updated.stockQuantity()).isEqualTo(7);
            assertThat(variant.stockQuantity()).isEqualTo(10);  // 원본 불변
        }

        @Test
        @DisplayName("재고 부족 시 예외")
        void decreaseStockThrowsWhenNotEnough() {
            // given
            ProductVariant variant = ProductVariant.of("옵션", "SKU", Map.of(), BigDecimal.ZERO, 5);

            // when & then
            assertThatThrownBy(() -> variant.decreaseStock(10))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Not enough stock");
        }

        @Test
        @DisplayName("재고 추가")
        void increaseStock() {
            // given
            ProductVariant variant = ProductVariant.of("옵션", "SKU", Map.of(), BigDecimal.ZERO, 10);

            // when
            ProductVariant updated = variant.increaseStock(5);

            // then
            assertThat(updated.stockQuantity()).isEqualTo(15);
            assertThat(variant.stockQuantity()).isEqualTo(10);  // 원본 불변
        }

        @Test
        @DisplayName("재고 전량 차감")
        void decreaseAllStock() {
            // given
            ProductVariant variant = ProductVariant.of("옵션", "SKU", Map.of(), BigDecimal.ZERO, 10);

            // when
            ProductVariant updated = variant.decreaseStock(10);

            // then
            assertThat(updated.stockQuantity()).isEqualTo(0);
            assertThat(updated.hasStock()).isFalse();
        }
    }

    // =====================================================
    // 활성화 상태 관리 테스트
    // =====================================================

    @Nested
    @DisplayName("활성화 상태 관리")
    class ActivationManagement {

        @Test
        @DisplayName("비활성화")
        void deactivate() {
            // given
            ProductVariant variant = ProductVariant.of("옵션", "SKU", Map.of(), BigDecimal.ZERO, 10);
            assertThat(variant.isActive()).isTrue();

            // when
            ProductVariant deactivated = variant.deactivate();

            // then
            assertThat(deactivated.isActive()).isFalse();
            assertThat(variant.isActive()).isTrue();  // 원본 불변
        }

        @Test
        @DisplayName("활성화")
        void activate() {
            // given
            ProductVariant variant = new ProductVariant(
                    VARIANT_ID_1, "옵션", "SKU", Map.of(), BigDecimal.ZERO, 10, false
            );
            assertThat(variant.isActive()).isFalse();

            // when
            ProductVariant activated = variant.activate();

            // then
            assertThat(activated.isActive()).isTrue();
        }
    }

    // =====================================================
    // 불변성 테스트
    // =====================================================

    @Nested
    @DisplayName("불변성")
    class Immutability {

        @Test
        @DisplayName("재고 차감은 새 객체 반환")
        void decreaseStockReturnsNewObject() {
            // given
            ProductVariant original = ProductVariant.of("옵션", "SKU", Map.of(), BigDecimal.ZERO, 10);

            // when
            ProductVariant updated = original.decreaseStock(3);

            // then
            assertThat(updated).isNotSameAs(original);
            assertThat(original.stockQuantity()).isEqualTo(10);
            assertThat(updated.stockQuantity()).isEqualTo(7);
        }

        @Test
        @DisplayName("재고 추가는 새 객체 반환")
        void increaseStockReturnsNewObject() {
            // given
            ProductVariant original = ProductVariant.of("옵션", "SKU", Map.of(), BigDecimal.ZERO, 10);

            // when
            ProductVariant updated = original.increaseStock(5);

            // then
            assertThat(updated).isNotSameAs(original);
            assertThat(original.stockQuantity()).isEqualTo(10);
            assertThat(updated.stockQuantity()).isEqualTo(15);
        }

        @Test
        @DisplayName("비활성화는 새 객체 반환")
        void deactivateReturnsNewObject() {
            // given
            ProductVariant original = ProductVariant.of("옵션", "SKU", Map.of(), BigDecimal.ZERO, 10);

            // when
            ProductVariant deactivated = original.deactivate();

            // then
            assertThat(deactivated).isNotSameAs(original);
            assertThat(original.isActive()).isTrue();
            assertThat(deactivated.isActive()).isFalse();
        }

        @Test
        @DisplayName("다른 필드는 변경 시 유지됨")
        void otherFieldsPreservedOnChange() {
            // given
            ProductVariant original = ProductVariant.of(
                    "빨강/L",
                    "SKU-RED-L",
                    Map.of("색상", "빨강"),
                    new BigDecimal("5000"),
                    100
            );

            // when
            ProductVariant updated = original.decreaseStock(10);

            // then
            assertThat(updated.id()).isEqualTo(original.id());
            assertThat(updated.name()).isEqualTo(original.name());
            assertThat(updated.sku()).isEqualTo(original.sku());
            assertThat(updated.optionValues()).isEqualTo(original.optionValues());
            assertThat(updated.additionalPrice()).isEqualTo(original.additionalPrice());
            assertThat(updated.isActive()).isEqualTo(original.isActive());
        }
    }

    // =====================================================
    // 동등성 테스트
    // =====================================================

    @Nested
    @DisplayName("동등성")
    class Equality {

        @Test
        @DisplayName("동일한 값을 가진 변형 상품은 동등")
        void equalVariantsAreEqual() {
            // given
            ProductVariant variant1 = new ProductVariant(
                    VARIANT_ID_1, "옵션", "SKU", Map.of("색상", "빨강"), new BigDecimal("1000"), 10, true
            );
            ProductVariant variant2 = new ProductVariant(
                    VARIANT_ID_1, "옵션", "SKU", Map.of("색상", "빨강"), new BigDecimal("1000"), 10, true
            );

            // then
            assertThat(variant1).isEqualTo(variant2);
            assertThat(variant1.hashCode()).isEqualTo(variant2.hashCode());
        }

        @Test
        @DisplayName("다른 ID를 가진 변형 상품은 비동등")
        void differentIdsAreNotEqual() {
            // given
            ProductVariant variant1 = new ProductVariant(
                    VARIANT_ID_1, "옵션", "SKU", Map.of(), BigDecimal.ZERO, 10, true
            );
            ProductVariant variant2 = new ProductVariant(
                    VARIANT_ID_2, "옵션", "SKU", Map.of(), BigDecimal.ZERO, 10, true
            );

            // then
            assertThat(variant1).isNotEqualTo(variant2);
        }
    }
}
