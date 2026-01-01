package jjh.delivery.application.service;

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

/**
 * 장바구니 서비스
 */
@Service
@Transactional
public class CartService implements ManageCartUseCase {

    private final LoadCartPort loadCartPort;
    private final SaveCartPort saveCartPort;
    private final LoadProductPort loadProductPort;

    public CartService(
            LoadCartPort loadCartPort,
            SaveCartPort saveCartPort,
            LoadProductPort loadProductPort
    ) {
        this.loadCartPort = loadCartPort;
        this.saveCartPort = saveCartPort;
        this.loadProductPort = loadProductPort;
    }

    @Override
    @Transactional(readOnly = true)
    public Cart getCart(String customerId) {
        return loadCartPort.findByCustomerId(customerId)
                .orElseGet(() -> Cart.createEmpty(customerId));
    }

    @Override
    public CartItem addItem(String customerId, AddCartItemCommand command) {
        // 상품 조회 및 판매 가능 여부 확인 (Optional 체이닝)
        Product product = loadProductPort.findById(command.productId())
                .filter(Product::isSellable)
                .orElseThrow(() -> new ProductNotFoundException(command.productId()));

        // 변형 상품 조회 (Optional 활용)
        Optional<ProductVariant> variantOpt = Optional.ofNullable(command.variantId())
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
                .map(v -> product.calculatePrice(command.variantId()))
                .orElse(product.getBasePrice());
        String thumbnailUrl = product.getImageUrls().stream().findFirst().orElse(null);

        // 장바구니 조회 또는 생성
        Cart cart = loadCartPort.findByCustomerId(customerId)
                .orElseGet(() -> Cart.createEmpty(customerId));

        // 상품 추가
        CartItem item = cart.addItem(
                command.productId(),
                product.getName(),
                command.variantId(),
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
    public CartItem updateItemQuantity(String customerId, String itemId, int quantity) {
        Cart cart = loadCartPort.findByCustomerId(customerId)
                .orElseThrow(() -> new CartItemNotFoundException(itemId));

        CartItem item = cart.updateItemQuantity(itemId, quantity);
        saveCartPort.save(cart);

        return item;
    }

    @Override
    public void removeItem(String customerId, String itemId) {
        Cart cart = loadCartPort.findByCustomerId(customerId)
                .orElseThrow(() -> new CartItemNotFoundException(itemId));

        cart.removeItem(itemId);
        saveCartPort.save(cart);
    }

    @Override
    public void clearCart(String customerId) {
        loadCartPort.findByCustomerId(customerId).ifPresent(cart -> {
            cart.clear();
            saveCartPort.save(cart);
        });
    }
}
