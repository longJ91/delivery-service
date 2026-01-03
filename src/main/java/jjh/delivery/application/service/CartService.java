package jjh.delivery.application.service;

import lombok.RequiredArgsConstructor;

import jjh.delivery.application.port.in.ManageCartUseCase;
import jjh.delivery.application.port.out.LoadCartPort;
import jjh.delivery.application.port.out.LoadProductPort;
import jjh.delivery.application.port.out.SaveCartPort;
import jjh.delivery.domain.cart.Cart;
import jjh.delivery.domain.cart.CartItem;
import jjh.delivery.domain.cart.exception.CartItemNotFoundException;
import jjh.delivery.domain.product.Product;
import jjh.delivery.domain.product.ProductVariant;
import jjh.delivery.domain.product.exception.ProductNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

/**
 * 장바구니 서비스
 */
@Service
@Transactional
@RequiredArgsConstructor
public class CartService implements ManageCartUseCase {

    private final LoadCartPort loadCartPort;
    private final SaveCartPort saveCartPort;
    private final LoadProductPort loadProductPort;

    @Override
    @Transactional(readOnly = true)
    public Cart getCart(UUID customerId) {
        return loadCartPort.findByCustomerId(customerId)
                .orElseGet(() -> Cart.createEmpty(customerId));
    }

    @Override
    public CartItem addItem(UUID customerId, AddCartItemCommand command) {
        UUID productId = UUID.fromString(command.productId());
        UUID variantId = command.variantId() != null ? UUID.fromString(command.variantId()) : null;

        // 상품 조회 및 판매 가능 여부 확인 (Optional 체이닝)
        Product product = loadProductPort.findById(productId)
                .filter(Product::isSellable)
                .orElseThrow(() -> new ProductNotFoundException(command.productId()));

        // 변형 상품 조회 (Optional 활용)
        Optional<ProductVariant> variantOpt = Optional.ofNullable(variantId)
                .flatMap(product::findVariant)
                .map(variant -> {
                    if (!variant.hasStock()) {
                        throw new IllegalStateException("Variant is out of stock: " + command.variantId());
                    }
                    return variant;
                });

        // 가격 및 이름 결정 (함수형 추출)
        String variantName = variantOpt.map(ProductVariant::name).orElse(null);
        BigDecimal unitPrice = variantOpt
                .map(v -> product.calculatePrice(variantId))
                .orElse(product.getBasePrice());
        String thumbnailUrl = product.getImageUrls().stream().findFirst().orElse(null);

        // 장바구니 조회 또는 생성
        Cart cart = loadCartPort.findByCustomerId(customerId)
                .orElseGet(() -> Cart.createEmpty(customerId));

        // 상품 추가
        CartItem item = cart.addItem(
                productId,
                product.getName(),
                variantId,
                variantName,
                product.getSellerId(),
                command.quantity(),
                unitPrice,
                thumbnailUrl
        );

        // 저장
        saveCartPort.save(cart);

        return item;
    }

    @Override
    public CartItem updateItemQuantity(UUID customerId, UUID itemId, int quantity) {
        Cart cart = loadCartPort.findByCustomerId(customerId)
                .orElseThrow(() -> new CartItemNotFoundException(itemId.toString()));

        CartItem item = cart.updateItemQuantity(itemId, quantity);
        saveCartPort.save(cart);

        return item;
    }

    @Override
    public void removeItem(UUID customerId, UUID itemId) {
        Cart cart = loadCartPort.findByCustomerId(customerId)
                .orElseThrow(() -> new CartItemNotFoundException(itemId.toString()));

        cart.removeItem(itemId);
        saveCartPort.save(cart);
    }

    @Override
    public void clearCart(UUID customerId) {
        loadCartPort.findByCustomerId(customerId).ifPresent(cart -> {
            cart.clear();
            saveCartPort.save(cart);
        });
    }
}
