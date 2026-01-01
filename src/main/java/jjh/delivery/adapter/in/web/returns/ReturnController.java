package jjh.delivery.adapter.in.web.returns;

import jakarta.validation.Valid;
import jjh.delivery.adapter.in.web.returns.dto.*;
import jjh.delivery.application.port.in.ManageReturnUseCase;
import jjh.delivery.application.port.in.ManageReturnUseCase.*;
import jjh.delivery.domain.returns.ProductReturn;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Return REST Controller - Driving Adapter (Inbound)
 * 반품/교환 관리 API
 */
@RestController
@RequestMapping("/api/v2/returns")
public class ReturnController {

    private final ManageReturnUseCase manageReturnUseCase;

    public ReturnController(ManageReturnUseCase manageReturnUseCase) {
        this.manageReturnUseCase = manageReturnUseCase;
    }

    /**
     * 반품 요청
     */
    @PostMapping
    public ResponseEntity<ReturnResponse> requestReturn(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody RequestReturnRequest request
    ) {
        String customerId = userDetails.getUsername();

        List<ReturnItemCommand> items = request.items().stream()
                .map(item -> new ReturnItemCommand(
                        item.orderItemId(),
                        item.productId(),
                        item.productName(),
                        item.variantId(),
                        item.variantName(),
                        item.quantity(),
                        item.refundAmount()
                ))
                .toList();

        RequestReturnCommand command = new RequestReturnCommand(
                customerId,
                request.orderId(),
                request.returnType(),
                request.reason(),
                request.reasonDetail(),
                items
        );

        ProductReturn productReturn = manageReturnUseCase.requestReturn(command);

        return ResponseEntity.status(HttpStatus.CREATED).body(ReturnResponse.from(productReturn));
    }

    /**
     * 반품 조회
     */
    @GetMapping("/{returnId}")
    public ResponseEntity<ReturnResponse> getReturn(
            @PathVariable String returnId
    ) {
        ProductReturn productReturn = manageReturnUseCase.getReturn(returnId);

        return ResponseEntity.ok(ReturnResponse.from(productReturn));
    }

    /**
     * 내 반품 목록 조회
     */
    @GetMapping("/my")
    public ResponseEntity<ReturnListResponse> getMyReturns(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String customerId = userDetails.getUsername();
        List<ProductReturn> returns = manageReturnUseCase.getReturnsByCustomerId(customerId);

        return ResponseEntity.ok(ReturnListResponse.from(returns));
    }

    /**
     * 주문별 반품 목록 조회
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<ReturnListResponse> getReturnsByOrderId(
            @PathVariable String orderId
    ) {
        List<ProductReturn> returns = manageReturnUseCase.getReturnsByOrderId(orderId);

        return ResponseEntity.ok(ReturnListResponse.from(returns));
    }

    /**
     * 반품 취소
     */
    @PostMapping("/{returnId}/cancel")
    public ResponseEntity<ReturnResponse> cancelReturn(
            @PathVariable String returnId
    ) {
        ProductReturn productReturn = manageReturnUseCase.cancelReturn(returnId);

        return ResponseEntity.ok(ReturnResponse.from(productReturn));
    }

    // ==================== Admin Endpoints ====================

    /**
     * 반품 승인 (관리자)
     */
    @PostMapping("/{returnId}/approve")
    public ResponseEntity<ReturnResponse> approveReturn(
            @PathVariable String returnId
    ) {
        ProductReturn productReturn = manageReturnUseCase.approveReturn(returnId);

        return ResponseEntity.ok(ReturnResponse.from(productReturn));
    }

    /**
     * 반품 거절 (관리자)
     */
    @PostMapping("/{returnId}/reject")
    public ResponseEntity<ReturnResponse> rejectReturn(
            @PathVariable String returnId,
            @Valid @RequestBody RejectReturnRequest request
    ) {
        ProductReturn productReturn = manageReturnUseCase.rejectReturn(returnId, request.reason());

        return ResponseEntity.ok(ReturnResponse.from(productReturn));
    }

    /**
     * 수거 예정 등록 (관리자)
     */
    @PostMapping("/{returnId}/schedule-pickup")
    public ResponseEntity<ReturnResponse> schedulePickup(
            @PathVariable String returnId
    ) {
        ProductReturn productReturn = manageReturnUseCase.schedulePickup(returnId);

        return ResponseEntity.ok(ReturnResponse.from(productReturn));
    }

    /**
     * 수거 완료 (관리자)
     */
    @PostMapping("/{returnId}/complete-pickup")
    public ResponseEntity<ReturnResponse> completePickup(
            @PathVariable String returnId
    ) {
        ProductReturn productReturn = manageReturnUseCase.completePickup(returnId);

        return ResponseEntity.ok(ReturnResponse.from(productReturn));
    }

    /**
     * 검수 시작 (관리자)
     */
    @PostMapping("/{returnId}/start-inspection")
    public ResponseEntity<ReturnResponse> startInspection(
            @PathVariable String returnId
    ) {
        ProductReturn productReturn = manageReturnUseCase.startInspection(returnId);

        return ResponseEntity.ok(ReturnResponse.from(productReturn));
    }

    /**
     * 반품 완료 (관리자)
     */
    @PostMapping("/{returnId}/complete")
    public ResponseEntity<ReturnResponse> completeReturn(
            @PathVariable String returnId
    ) {
        ProductReturn productReturn = manageReturnUseCase.completeReturn(returnId);

        return ResponseEntity.ok(ReturnResponse.from(productReturn));
    }
}
