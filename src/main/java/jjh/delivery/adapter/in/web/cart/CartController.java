package jjh.delivery.adapter.in.web.cart;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import jjh.delivery.adapter.in.web.cart.dto.*;
import jjh.delivery.application.port.in.ManageCartUseCase;
import jjh.delivery.application.port.in.ManageCartUseCase.AddCartItemCommand;
import jjh.delivery.application.port.out.LoadProductPort;
import jjh.delivery.application.port.out.LoadSellerInfoPort;
import jjh.delivery.domain.cart.Cart;
import jjh.delivery.domain.cart.CartItem;
import jjh.delivery.domain.product.Product;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Cart REST Controller - Driving Adapter (Inbound)
 * 장바구니 관리 API
 */
@RestController
@RequestMapping("/api/v2/cart")
@RequiredArgsConstructor
public class CartController {

    private final ManageCartUseCase manageCartUseCase;
    private final LoadProductPort loadProductPort;
    private final LoadSellerInfoPort loadSellerInfoPort;

    /**
     * 장바구니 조회
     */
    @GetMapping
    public ResponseEntity<CartResponse> getCart(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID customerId = UUID.fromString(userDetails.getUsername());
        Cart cart = manageCartUseCase.getCart(customerId);

        List<CartItemResponse> items = cart.getItems().stream()
                .map(this::toItemResponse)
                .toList();

        return ResponseEntity.ok(CartResponse.of(items, cart.getTotalAmount(), cart.getTotalItems()));
    }

    /**
     * 장바구니 상품 추가
     */
    @PostMapping("/items")
    public ResponseEntity<Void> addItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AddCartItemRequest request
    ) {
        UUID customerId = UUID.fromString(userDetails.getUsername());

        AddCartItemCommand command = new AddCartItemCommand(
                request.productId(),
                request.variantId(),
                request.quantity()
        );

        manageCartUseCase.addItem(customerId, command);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 장바구니 수량 변경
     */
    @PatchMapping("/items/{itemId}")
    public ResponseEntity<Void> updateItemQuantity(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID itemId,
            @Valid @RequestBody UpdateCartItemRequest request
    ) {
        UUID customerId = UUID.fromString(userDetails.getUsername());
        manageCartUseCase.updateItemQuantity(customerId, itemId, request.quantity());
        return ResponseEntity.ok().build();
    }

    /**
     * 장바구니 상품 삭제
     */
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> removeItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID itemId
    ) {
        UUID customerId = UUID.fromString(userDetails.getUsername());
        manageCartUseCase.removeItem(customerId, itemId);
        return ResponseEntity.noContent().build();
    }

    // ==================== Private Methods ====================

    private CartItemResponse toItemResponse(CartItem item) {
        String sellerName = loadSellerInfoPort.findBusinessNameById(item.sellerId()).orElse("Unknown");

        // 재고 및 가용성 확인
        int stock = 0;
        boolean isAvailable = false;

        Product product = loadProductPort.findById(item.productId()).orElse(null);
        if (product != null) {
            if (item.variantId() != null) {
                var variant = product.findVariant(item.variantId());
                if (variant.isPresent()) {
                    stock = variant.get().stockQuantity();
                    isAvailable = variant.get().hasStock() && product.isSellable();
                }
            } else {
                stock = product.getTotalStockQuantity();
                isAvailable = product.isSellable();
            }
        }

        return CartItemResponse.from(item, sellerName, stock, isAvailable);
    }
}
