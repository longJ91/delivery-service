package jjh.delivery.application.service;

import jjh.delivery.application.port.in.ManageCartUseCase.AddCartItemCommand;
import jjh.delivery.application.port.out.LoadCartPort;
import jjh.delivery.application.port.out.LoadProductPort;
import jjh.delivery.application.port.out.SaveCartPort;
import jjh.delivery.domain.cart.Cart;
import jjh.delivery.domain.cart.CartItem;
import jjh.delivery.domain.cart.exception.CartItemNotFoundException;
import jjh.delivery.domain.product.Product;
import jjh.delivery.domain.product.ProductStatus;
import jjh.delivery.domain.product.ProductVariant;
import jjh.delivery.domain.product.exception.ProductNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * CartService Unit Tests
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CartService 테스트")
class CartServiceTest {

    @Mock
    private LoadCartPort loadCartPort;

    @Mock
    private SaveCartPort saveCartPort;

    @Mock
    private LoadProductPort loadProductPort;

    @InjectMocks
    private CartService cartService;

    // =====================================================
    // Test Fixtures
    // =====================================================

    private static final String CUSTOMER_ID = "customer-123";
    private static final String PRODUCT_ID = "product-456";
    private static final String SELLER_ID = "seller-789";

    private Product createActiveProduct() {
        return Product.builder()
                .sellerId(SELLER_ID)
                .name("테스트 상품")
                .description("테스트 상품 설명")
                .basePrice(new BigDecimal("10000"))
                .status(ProductStatus.ACTIVE)
                .variants(List.of(
                        ProductVariant.of("기본", "SKU-DEFAULT", Map.of(), BigDecimal.ZERO, 100)
                ))
                .imageUrls(List.of("http://example.com/image.jpg"))
                .build();
    }

    private Product createActiveProductWithVariants() {
        return Product.builder()
                .sellerId(SELLER_ID)
                .name("테스트 상품")
                .description("테스트 상품 설명")
                .basePrice(new BigDecimal("10000"))
                .status(ProductStatus.ACTIVE)
                .variants(List.of(
                        ProductVariant.of("빨강/L", "SKU-RED-L", Map.of("색상", "빨강", "사이즈", "L"), new BigDecimal("2000"), 50)
                ))
                .imageUrls(List.of("http://example.com/image.jpg"))
                .build();
    }

    private Cart createEmptyCart() {
        return Cart.createEmpty(CUSTOMER_ID);
    }

    private Cart createCartWithItem() {
        Cart cart = Cart.createEmpty(CUSTOMER_ID);
        cart.addItem(PRODUCT_ID, "상품", null, null, SELLER_ID, 2, new BigDecimal("10000"), null);
        return cart;
    }

    // =====================================================
    // 장바구니 조회 테스트
    // =====================================================

    @Nested
    @DisplayName("장바구니 조회")
    class GetCart {

        @Test
        @DisplayName("기존 장바구니 조회")
        void getExistingCartSuccess() {
            // given
            Cart cart = createCartWithItem();

            given(loadCartPort.findByCustomerId(CUSTOMER_ID))
                    .willReturn(Optional.of(cart));

            // when
            Cart result = cartService.getCart(CUSTOMER_ID);

            // then
            assertThat(result).isEqualTo(cart);
            assertThat(result.getItems()).hasSize(1);
        }

        @Test
        @DisplayName("장바구니 없으면 새로 생성")
        void getCartCreatesNewWhenNotExists() {
            // given
            given(loadCartPort.findByCustomerId(CUSTOMER_ID))
                    .willReturn(Optional.empty());

            // when
            Cart result = cartService.getCart(CUSTOMER_ID);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getCustomerId()).isEqualTo(CUSTOMER_ID);
            assertThat(result.isEmpty()).isTrue();
        }
    }

    // =====================================================
    // 상품 추가 테스트
    // =====================================================

    @Nested
    @DisplayName("상품 추가")
    class AddItem {

        @Test
        @DisplayName("새 장바구니에 상품 추가")
        void addItemToNewCartSuccess() {
            // given
            Product product = createActiveProduct();
            AddCartItemCommand command = new AddCartItemCommand(product.getId(), null, 2);

            given(loadProductPort.findById(product.getId()))
                    .willReturn(Optional.of(product));
            given(loadCartPort.findByCustomerId(CUSTOMER_ID))
                    .willReturn(Optional.empty());

            // when
            CartItem result = cartService.addItem(CUSTOMER_ID, command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.productId()).isEqualTo(product.getId());
            assertThat(result.quantity()).isEqualTo(2);
            assertThat(result.unitPrice()).isEqualByComparingTo(new BigDecimal("10000"));
            verify(saveCartPort).save(any(Cart.class));
        }

        @Test
        @DisplayName("기존 장바구니에 상품 추가")
        void addItemToExistingCartSuccess() {
            // given
            Product product = createActiveProduct();
            Cart cart = createEmptyCart();
            AddCartItemCommand command = new AddCartItemCommand(product.getId(), null, 3);

            given(loadProductPort.findById(product.getId()))
                    .willReturn(Optional.of(product));
            given(loadCartPort.findByCustomerId(CUSTOMER_ID))
                    .willReturn(Optional.of(cart));

            // when
            CartItem result = cartService.addItem(CUSTOMER_ID, command);

            // then
            assertThat(result.quantity()).isEqualTo(3);
            verify(saveCartPort).save(cart);
        }

        @Test
        @DisplayName("변형 상품 추가")
        void addItemWithVariantSuccess() {
            // given
            Product product = createActiveProductWithVariants();
            String variantId = product.getVariants().get(0).id();
            AddCartItemCommand command = new AddCartItemCommand(product.getId(), variantId, 1);

            given(loadProductPort.findById(product.getId()))
                    .willReturn(Optional.of(product));
            given(loadCartPort.findByCustomerId(CUSTOMER_ID))
                    .willReturn(Optional.empty());

            // when
            CartItem result = cartService.addItem(CUSTOMER_ID, command);

            // then
            assertThat(result.variantId()).isEqualTo(variantId);
            assertThat(result.variantName()).isEqualTo("빨강/L");
            // 기본가 10000 + 추가가격 2000 = 12000
            assertThat(result.unitPrice()).isEqualByComparingTo(new BigDecimal("12000"));
        }

        @Test
        @DisplayName("존재하지 않는 상품 추가 시 예외")
        void addItemProductNotFoundThrowsException() {
            // given
            AddCartItemCommand command = new AddCartItemCommand("non-existent", null, 1);

            given(loadProductPort.findById("non-existent"))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> cartService.addItem(CUSTOMER_ID, command))
                    .isInstanceOf(ProductNotFoundException.class);

            verify(saveCartPort, never()).save(any());
        }

        @Test
        @DisplayName("판매 불가능 상품 추가 시 예외")
        void addItemUnsellableProductThrowsException() {
            // given
            Product product = Product.builder()
                    .sellerId(SELLER_ID)
                    .name("테스트 상품")
                    .description("설명")
                    .basePrice(new BigDecimal("10000"))
                    .status(ProductStatus.INACTIVE)
                    .variants(List.of(
                            ProductVariant.of("기본", "SKU-DEFAULT", Map.of(), BigDecimal.ZERO, 100)
                    ))
                    .build();
            AddCartItemCommand command = new AddCartItemCommand(product.getId(), null, 1);

            given(loadProductPort.findById(product.getId()))
                    .willReturn(Optional.of(product));

            // when & then
            assertThatThrownBy(() -> cartService.addItem(CUSTOMER_ID, command))
                    .isInstanceOf(ProductNotFoundException.class);

            verify(saveCartPort, never()).save(any());
        }

        @Test
        @DisplayName("재고 없는 변형 상품 추가 시 예외")
        void addItemOutOfStockVariantThrowsException() {
            // given - 상품에 재고 있는 variant와 없는 variant가 있어야 상품 자체는 판매 가능
            ProductVariant outOfStockVariant = ProductVariant.of("빨강/L", "SKU-RED-L", Map.of(), BigDecimal.ZERO, 0);
            ProductVariant inStockVariant = ProductVariant.of("파랑/M", "SKU-BLUE-M", Map.of(), BigDecimal.ZERO, 50);

            Product product = Product.builder()
                    .sellerId(SELLER_ID)
                    .name("테스트 상품")
                    .description("설명")
                    .basePrice(new BigDecimal("10000"))
                    .status(ProductStatus.ACTIVE)
                    .variants(List.of(inStockVariant, outOfStockVariant))
                    .build();
            AddCartItemCommand command = new AddCartItemCommand(product.getId(), outOfStockVariant.id(), 1);

            given(loadProductPort.findById(product.getId()))
                    .willReturn(Optional.of(product));

            // when & then
            assertThatThrownBy(() -> cartService.addItem(CUSTOMER_ID, command))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("out of stock");

            verify(saveCartPort, never()).save(any());
        }
    }

    // =====================================================
    // 수량 변경 테스트
    // =====================================================

    @Nested
    @DisplayName("수량 변경")
    class UpdateItemQuantity {

        @Test
        @DisplayName("수량 변경 성공")
        void updateItemQuantitySuccess() {
            // given
            Cart cart = createCartWithItem();
            String itemId = cart.getItems().get(0).id();

            given(loadCartPort.findByCustomerId(CUSTOMER_ID))
                    .willReturn(Optional.of(cart));

            // when
            CartItem result = cartService.updateItemQuantity(CUSTOMER_ID, itemId, 5);

            // then
            assertThat(result.quantity()).isEqualTo(5);
            verify(saveCartPort).save(cart);
        }

        @Test
        @DisplayName("장바구니 없을 때 수량 변경 시 예외")
        void updateItemQuantityCartNotFoundThrowsException() {
            // given
            given(loadCartPort.findByCustomerId(CUSTOMER_ID))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> cartService.updateItemQuantity(CUSTOMER_ID, "item-id", 5))
                    .isInstanceOf(CartItemNotFoundException.class);

            verify(saveCartPort, never()).save(any());
        }

        @Test
        @DisplayName("존재하지 않는 항목 수량 변경 시 예외")
        void updateNonExistentItemThrowsException() {
            // given
            Cart cart = createEmptyCart();

            given(loadCartPort.findByCustomerId(CUSTOMER_ID))
                    .willReturn(Optional.of(cart));

            // when & then
            assertThatThrownBy(() -> cartService.updateItemQuantity(CUSTOMER_ID, "non-existent", 5))
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
        void removeItemSuccess() {
            // given
            Cart cart = createCartWithItem();
            String itemId = cart.getItems().get(0).id();

            given(loadCartPort.findByCustomerId(CUSTOMER_ID))
                    .willReturn(Optional.of(cart));

            // when
            cartService.removeItem(CUSTOMER_ID, itemId);

            // then
            assertThat(cart.getItems()).isEmpty();
            verify(saveCartPort).save(cart);
        }

        @Test
        @DisplayName("장바구니 없을 때 제거 시 예외")
        void removeItemCartNotFoundThrowsException() {
            // given
            given(loadCartPort.findByCustomerId(CUSTOMER_ID))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> cartService.removeItem(CUSTOMER_ID, "item-id"))
                    .isInstanceOf(CartItemNotFoundException.class);

            verify(saveCartPort, never()).save(any());
        }
    }

    // =====================================================
    // 장바구니 비우기 테스트
    // =====================================================

    @Nested
    @DisplayName("장바구니 비우기")
    class ClearCart {

        @Test
        @DisplayName("장바구니 비우기 성공")
        void clearCartSuccess() {
            // given
            Cart cart = createCartWithItem();
            assertThat(cart.isEmpty()).isFalse();

            given(loadCartPort.findByCustomerId(CUSTOMER_ID))
                    .willReturn(Optional.of(cart));

            // when
            cartService.clearCart(CUSTOMER_ID);

            // then
            assertThat(cart.isEmpty()).isTrue();
            verify(saveCartPort).save(cart);
        }

        @Test
        @DisplayName("존재하지 않는 장바구니 비우기는 무시")
        void clearNonExistentCartDoesNothing() {
            // given
            given(loadCartPort.findByCustomerId(CUSTOMER_ID))
                    .willReturn(Optional.empty());

            // when
            cartService.clearCart(CUSTOMER_ID);

            // then
            verify(saveCartPort, never()).save(any());
        }
    }
}
