package jjh.delivery.domain.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * OrderItem Value Object Unit Tests
 */
@DisplayName("OrderItem 도메인 테스트")
class OrderItemTest {

    // =====================================================
    // 생성 테스트
    // =====================================================

    @Nested
    @DisplayName("OrderItem 생성")
    class OrderItemCreation {

        @Test
        @DisplayName("간단한 상품으로 OrderItem 생성")
        void createSimpleOrderItem() {
            // given & when
            OrderItem item = OrderItem.of(
                    "product-123",
                    "테스트 상품",
                    2,
                    new BigDecimal("15000")
            );

            // then
            assertThat(item.productId()).isEqualTo("product-123");
            assertThat(item.productName()).isEqualTo("테스트 상품");
            assertThat(item.quantity()).isEqualTo(2);
            assertThat(item.unitPrice()).isEqualByComparingTo(new BigDecimal("15000"));
            assertThat(item.variantId()).isNull();
            assertThat(item.hasVariant()).isFalse();
        }

        @Test
        @DisplayName("변형 상품으로 OrderItem 생성")
        void createVariantOrderItem() {
            // given
            Map<String, String> options = Map.of("색상", "빨강", "사이즈", "L");

            // when
            OrderItem item = OrderItem.ofVariant(
                    "product-123",
                    "테스트 상품",
                    "variant-456",
                    "빨강/L",
                    "SKU-RED-L",
                    options,
                    3,
                    new BigDecimal("20000")
            );

            // then
            assertThat(item.productId()).isEqualTo("product-123");
            assertThat(item.variantId()).isEqualTo("variant-456");
            assertThat(item.variantName()).isEqualTo("빨강/L");
            assertThat(item.sku()).isEqualTo("SKU-RED-L");
            assertThat(item.optionValues()).containsEntry("색상", "빨강");
            assertThat(item.hasVariant()).isTrue();
        }

        @Test
        @DisplayName("productId 없이 생성 시 예외 발생")
        void createWithoutProductIdThrowsException() {
            assertThatThrownBy(() ->
                    OrderItem.of(null, "상품명", 1, new BigDecimal("10000"))
            ).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("productId");
        }

        @Test
        @DisplayName("빈 productId로 생성 시 예외 발생")
        void createWithBlankProductIdThrowsException() {
            assertThatThrownBy(() ->
                    OrderItem.of("  ", "상품명", 1, new BigDecimal("10000"))
            ).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("productId");
        }

        @Test
        @DisplayName("productName 없이 생성 시 예외 발생")
        void createWithoutProductNameThrowsException() {
            assertThatThrownBy(() ->
                    OrderItem.of("product-123", null, 1, new BigDecimal("10000"))
            ).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("productName");
        }

        @Test
        @DisplayName("수량이 0일 때 예외 발생")
        void createWithZeroQuantityThrowsException() {
            assertThatThrownBy(() ->
                    OrderItem.of("product-123", "상품명", 0, new BigDecimal("10000"))
            ).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("quantity");
        }

        @Test
        @DisplayName("수량이 음수일 때 예외 발생")
        void createWithNegativeQuantityThrowsException() {
            assertThatThrownBy(() ->
                    OrderItem.of("product-123", "상품명", -1, new BigDecimal("10000"))
            ).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("quantity");
        }

        @Test
        @DisplayName("단가가 null일 때 예외 발생")
        void createWithNullUnitPriceThrowsException() {
            assertThatThrownBy(() ->
                    OrderItem.of("product-123", "상품명", 1, null)
            ).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("unitPrice");
        }

        @Test
        @DisplayName("단가가 0일 때 예외 발생")
        void createWithZeroUnitPriceThrowsException() {
            assertThatThrownBy(() ->
                    OrderItem.of("product-123", "상품명", 1, BigDecimal.ZERO)
            ).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("unitPrice");
        }

        @Test
        @DisplayName("단가가 음수일 때 예외 발생")
        void createWithNegativeUnitPriceThrowsException() {
            assertThatThrownBy(() ->
                    OrderItem.of("product-123", "상품명", 1, new BigDecimal("-100"))
            ).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("unitPrice");
        }
    }

    // =====================================================
    // 금액 계산 테스트
    // =====================================================

    @Nested
    @DisplayName("금액 계산")
    class SubtotalCalculation {

        @Test
        @DisplayName("소계 금액 계산")
        void calculateSubtotal() {
            // given
            OrderItem item = OrderItem.of(
                    "product-123",
                    "테스트 상품",
                    3,
                    new BigDecimal("15000")
            );

            // when
            BigDecimal subtotal = item.calculateSubtotal();

            // then (3 * 15,000 = 45,000)
            assertThat(subtotal).isEqualByComparingTo(new BigDecimal("45000"));
        }

        @Test
        @DisplayName("수량 1개일 때 소계는 단가와 동일")
        void subtotalEqualsUnitPriceForSingleQuantity() {
            // given
            OrderItem item = OrderItem.of(
                    "product-123",
                    "테스트 상품",
                    1,
                    new BigDecimal("25000")
            );

            // when
            BigDecimal subtotal = item.calculateSubtotal();

            // then
            assertThat(subtotal).isEqualByComparingTo(item.unitPrice());
        }

        @Test
        @DisplayName("큰 수량에 대한 소계 계산")
        void calculateSubtotalForLargeQuantity() {
            // given
            OrderItem item = OrderItem.of(
                    "product-123",
                    "대량 상품",
                    1000,
                    new BigDecimal("9900")
            );

            // when
            BigDecimal subtotal = item.calculateSubtotal();

            // then (1000 * 9,900 = 9,900,000)
            assertThat(subtotal).isEqualByComparingTo(new BigDecimal("9900000"));
        }
    }

    // =====================================================
    // 변형 상품 확인 테스트
    // =====================================================

    @Nested
    @DisplayName("변형 상품 확인")
    class VariantCheck {

        @Test
        @DisplayName("variantId가 null이면 변형 상품 아님")
        void noVariantWhenVariantIdIsNull() {
            // given
            OrderItem item = OrderItem.of(
                    "product-123",
                    "상품",
                    1,
                    new BigDecimal("10000")
            );

            // then
            assertThat(item.hasVariant()).isFalse();
        }

        @Test
        @DisplayName("variantId가 빈 문자열이면 변형 상품 아님")
        void noVariantWhenVariantIdIsBlank() {
            // given
            OrderItem item = new OrderItem(
                    "product-123",
                    "상품",
                    "",  // blank variantId
                    null,
                    null,
                    null,
                    1,
                    new BigDecimal("10000")
            );

            // then
            assertThat(item.hasVariant()).isFalse();
        }

        @Test
        @DisplayName("variantId가 있으면 변형 상품")
        void hasVariantWhenVariantIdExists() {
            // given
            OrderItem item = OrderItem.ofVariant(
                    "product-123",
                    "상품",
                    "variant-456",
                    "옵션명",
                    "SKU-001",
                    null,
                    1,
                    new BigDecimal("10000")
            );

            // then
            assertThat(item.hasVariant()).isTrue();
        }
    }

    // =====================================================
    // 불변성 테스트
    // =====================================================

    @Nested
    @DisplayName("불변성")
    class Immutability {

        @Test
        @DisplayName("optionValues는 불변")
        void optionValuesIsImmutable() {
            // given
            Map<String, String> options = Map.of("색상", "빨강");
            OrderItem item = OrderItem.ofVariant(
                    "product-123",
                    "상품",
                    "variant-456",
                    "빨강",
                    "SKU-RED",
                    options,
                    1,
                    new BigDecimal("10000")
            );

            // when & then
            assertThatThrownBy(() -> item.optionValues().put("새옵션", "값"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("null optionValues는 빈 맵으로 변환")
        void nullOptionValuesBecomesEmptyMap() {
            // given
            OrderItem item = OrderItem.ofVariant(
                    "product-123",
                    "상품",
                    "variant-456",
                    "옵션명",
                    "SKU-001",
                    null,
                    1,
                    new BigDecimal("10000")
            );

            // then
            assertThat(item.optionValues()).isNotNull();
            assertThat(item.optionValues()).isEmpty();
        }
    }

    // =====================================================
    // Record 동등성 테스트
    // =====================================================

    @Nested
    @DisplayName("동등성")
    class Equality {

        @Test
        @DisplayName("동일한 값을 가진 OrderItem은 동등")
        void equalOrderItemsAreEqual() {
            // given
            OrderItem item1 = OrderItem.of("product-123", "상품", 2, new BigDecimal("10000"));
            OrderItem item2 = OrderItem.of("product-123", "상품", 2, new BigDecimal("10000"));

            // then
            assertThat(item1).isEqualTo(item2);
            assertThat(item1.hashCode()).isEqualTo(item2.hashCode());
        }

        @Test
        @DisplayName("다른 값을 가진 OrderItem은 비동등")
        void differentOrderItemsAreNotEqual() {
            // given
            OrderItem item1 = OrderItem.of("product-123", "상품", 2, new BigDecimal("10000"));
            OrderItem item2 = OrderItem.of("product-456", "상품", 2, new BigDecimal("10000"));

            // then
            assertThat(item1).isNotEqualTo(item2);
        }
    }
}
