package jjh.delivery.domain.cart;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * Cart Aggregate Root Unit Tests
 */
@DisplayName("Cart 도메인 테스트")
class CartTest {

    // =====================================================
    // Test Fixtures
    // =====================================================

    private Cart createEmptyCart() {
        return Cart.createEmpty("customer-123");
    }

    // =====================================================
    // 장바구니 생성 테스트
    // =====================================================

    @Nested
    @DisplayName("장바구니 생성")
    class CartCreation {

        @Test
        @DisplayName("빈 장바구니 생성")
        void createEmptyCart() {
            // when
            Cart cart = Cart.createEmpty("customer-123");

            // then
            assertThat(cart.getId()).isNotNull();
            assertThat(cart.getCustomerId()).isEqualTo("customer-123");
            assertThat(cart.getItems()).isEmpty();
            assertThat(cart.isEmpty()).isTrue();
            assertThat(cart.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("기존 장바구니 복원")
        void restoreCart() {
            // given
            var items = java.util.List.of(
                    CartItem.create("product-1", "상품1", null, null, "seller-1", 2, new BigDecimal("10000"), null)
            );

            // when
            Cart cart = Cart.restore("cart-id", "customer-123", items, java.time.LocalDateTime.now());

            // then
            assertThat(cart.getId()).isEqualTo("cart-id");
            assertThat(cart.getItems()).hasSize(1);
        }
    }

    // =====================================================
    // 상품 추가 테스트
    // =====================================================

    @Nested
    @DisplayName("상품 추가")
    class AddItem {

        @Test
        @DisplayName("새 상품 추가")
        void addNewItem() {
            // given
            Cart cart = createEmptyCart();

            // when
            CartItem item = cart.addItem(
                    "product-1",
                    "테스트 상품",
                    null,
                    null,
                    "seller-1",
                    2,
                    new BigDecimal("15000"),
                    "http://example.com/thumb.jpg"
            );

            // then
            assertThat(cart.getItems()).hasSize(1);
            assertThat(item.productId()).isEqualTo("product-1");
            assertThat(item.quantity()).isEqualTo(2);
            assertThat(cart.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("동일 상품 추가 시 수량 증가")
        void addExistingItemIncreasesQuantity() {
            // given
            Cart cart = createEmptyCart();
            cart.addItem("product-1", "상품", null, null, "seller-1", 2, new BigDecimal("10000"), null);

            // when
            cart.addItem("product-1", "상품", null, null, "seller-1", 3, new BigDecimal("10000"), null);

            // then
            assertThat(cart.getItems()).hasSize(1);
            assertThat(cart.getItems().get(0).quantity()).isEqualTo(5);
        }

        @Test
        @DisplayName("같은 상품 다른 옵션은 별도 항목으로 추가")
        void sameProductDifferentVariantAddsNewItem() {
            // given
            Cart cart = createEmptyCart();
            cart.addItem("product-1", "상품", "variant-1", "빨강", "seller-1", 1, new BigDecimal("10000"), null);

            // when
            cart.addItem("product-1", "상품", "variant-2", "파랑", "seller-1", 1, new BigDecimal("10000"), null);

            // then
            assertThat(cart.getItems()).hasSize(2);
        }
    }

    // =====================================================
    // 수량 변경 테스트
    // =====================================================

    @Nested
    @DisplayName("수량 변경")
    class UpdateQuantity {

        @Test
        @DisplayName("수량 변경 성공")
        void updateItemQuantity() {
            // given
            Cart cart = createEmptyCart();
            cart.addItem("product-1", "상품", null, null, "seller-1", 2, new BigDecimal("10000"), null);
            String itemId = cart.getItems().get(0).id();

            // when
            CartItem updated = cart.updateItemQuantity(itemId, 5);

            // then
            assertThat(updated.quantity()).isEqualTo(5);
            assertThat(cart.getItems().get(0).quantity()).isEqualTo(5);
        }

        @Test
        @DisplayName("존재하지 않는 항목 수량 변경 시 예외")
        void updateNonExistentItemThrowsException() {
            // given
            Cart cart = createEmptyCart();

            // when & then
            assertThatThrownBy(() -> cart.updateItemQuantity("non-existent", 5))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("not found");
        }
    }

    // =====================================================
    // 상품 제거 테스트
    // =====================================================

    @Nested
    @DisplayName("상품 제거")
    class RemoveItem {

        @Test
        @DisplayName("상품 제거 성공")
        void removeItem() {
            // given
            Cart cart = createEmptyCart();
            cart.addItem("product-1", "상품1", null, null, "seller-1", 1, new BigDecimal("10000"), null);
            cart.addItem("product-2", "상품2", null, null, "seller-1", 1, new BigDecimal("20000"), null);
            String itemId = cart.getItems().get(0).id();

            // when
            cart.removeItem(itemId);

            // then
            assertThat(cart.getItems()).hasSize(1);
            assertThat(cart.getItems().get(0).productId()).isEqualTo("product-2");
        }

        @Test
        @DisplayName("존재하지 않는 항목 제거 시 예외")
        void removeNonExistentItemThrowsException() {
            // given
            Cart cart = createEmptyCart();

            // when & then
            assertThatThrownBy(() -> cart.removeItem("non-existent"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("not found");
        }

        @Test
        @DisplayName("장바구니 비우기")
        void clearCart() {
            // given
            Cart cart = createEmptyCart();
            cart.addItem("product-1", "상품1", null, null, "seller-1", 1, new BigDecimal("10000"), null);
            cart.addItem("product-2", "상품2", null, null, "seller-1", 1, new BigDecimal("20000"), null);

            // when
            cart.clear();

            // then
            assertThat(cart.getItems()).isEmpty();
            assertThat(cart.isEmpty()).isTrue();
        }
    }

    // =====================================================
    // 금액 계산 테스트
    // =====================================================

    @Nested
    @DisplayName("금액 계산")
    class AmountCalculation {

        @Test
        @DisplayName("총 금액 계산")
        void getTotalAmount() {
            // given
            Cart cart = createEmptyCart();
            cart.addItem("product-1", "상품1", null, null, "seller-1", 2, new BigDecimal("10000"), null);  // 20,000
            cart.addItem("product-2", "상품2", null, null, "seller-1", 3, new BigDecimal("5000"), null);   // 15,000

            // then
            assertThat(cart.getTotalAmount()).isEqualByComparingTo(new BigDecimal("35000"));
        }

        @Test
        @DisplayName("빈 장바구니 총 금액은 0")
        void emptyCartTotalAmountIsZero() {
            // given
            Cart cart = createEmptyCart();

            // then
            assertThat(cart.getTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("총 상품 수 계산")
        void getTotalItems() {
            // given
            Cart cart = createEmptyCart();
            cart.addItem("product-1", "상품1", null, null, "seller-1", 2, new BigDecimal("10000"), null);
            cart.addItem("product-2", "상품2", null, null, "seller-1", 3, new BigDecimal("5000"), null);

            // then
            assertThat(cart.getTotalItems()).isEqualTo(5);
        }
    }

    // =====================================================
    // 조회 메서드 테스트
    // =====================================================

    @Nested
    @DisplayName("조회 메서드")
    class QueryMethods {

        @Test
        @DisplayName("ID로 항목 조회")
        void findItemById() {
            // given
            Cart cart = createEmptyCart();
            cart.addItem("product-1", "상품", null, null, "seller-1", 1, new BigDecimal("10000"), null);
            String itemId = cart.getItems().get(0).id();

            // then
            assertThat(cart.findItemById(itemId)).isPresent();
            assertThat(cart.findItemById("non-existent")).isEmpty();
        }

        @Test
        @DisplayName("상품+옵션으로 항목 조회")
        void findItemByProductAndVariant() {
            // given
            Cart cart = createEmptyCart();
            cart.addItem("product-1", "상품", "variant-1", "빨강", "seller-1", 1, new BigDecimal("10000"), null);

            // then
            assertThat(cart.findItemByProductAndVariant("product-1", "variant-1")).isPresent();
            assertThat(cart.findItemByProductAndVariant("product-1", "variant-2")).isEmpty();
            assertThat(cart.findItemByProductAndVariant("product-2", "variant-1")).isEmpty();
        }

        @Test
        @DisplayName("옵션 없는 상품 조회")
        void findItemWithoutVariant() {
            // given
            Cart cart = createEmptyCart();
            cart.addItem("product-1", "상품", null, null, "seller-1", 1, new BigDecimal("10000"), null);

            // then
            assertThat(cart.findItemByProductAndVariant("product-1", null)).isPresent();
        }

        @Test
        @DisplayName("항목 목록은 불변")
        void itemsListIsImmutable() {
            // given
            Cart cart = createEmptyCart();
            cart.addItem("product-1", "상품", null, null, "seller-1", 1, new BigDecimal("10000"), null);

            // when & then
            assertThatThrownBy(() -> cart.getItems().clear())
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }
}
