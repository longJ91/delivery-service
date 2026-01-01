package jjh.delivery.adapter.in.web.seller;

import jakarta.validation.Valid;
import jjh.delivery.adapter.in.web.seller.dto.*;
import jjh.delivery.application.port.in.ManageSellerUseCase;
import jjh.delivery.application.port.in.ManageSellerUseCase.*;
import jjh.delivery.domain.seller.Seller;
import jjh.delivery.domain.seller.SellerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * Seller Admin REST Controller - Driving Adapter (Inbound)
 * 판매자 관리 API
 */
@RestController
@RequestMapping("/api/v2/sellers")
public class SellerAdminController {

    private final ManageSellerUseCase manageSellerUseCase;

    public SellerAdminController(ManageSellerUseCase manageSellerUseCase) {
        this.manageSellerUseCase = manageSellerUseCase;
    }

    // ==================== 판매자 등록 ====================

    /**
     * 판매자 등록 신청
     */
    @PostMapping("/register")
    public ResponseEntity<SellerResponse> registerSeller(
            @Valid @RequestBody RegisterSellerRequest request
    ) {
        RegisterSellerCommand command = new RegisterSellerCommand(
                request.businessName(),
                request.businessNumber(),
                request.representativeName(),
                request.email(),
                request.phoneNumber(),
                request.sellerType(),
                request.warehouseAddress() != null ? request.warehouseAddress().toCommand() : null,
                request.categoryIds()
        );

        Seller seller = manageSellerUseCase.registerSeller(command);

        return ResponseEntity.status(HttpStatus.CREATED).body(SellerResponse.from(seller));
    }

    // ==================== 판매자 정보 수정 ====================

    /**
     * 판매자 정보 수정
     */
    @PutMapping("/{sellerId}")
    public ResponseEntity<SellerResponse> updateSellerInfo(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String sellerId,
            @Valid @RequestBody UpdateSellerInfoRequest request
    ) {
        // TODO: 본인 확인 로직 추가 필요
        UpdateSellerInfoCommand command = new UpdateSellerInfoCommand(
                sellerId,
                request.businessName(),
                request.representativeName(),
                request.email(),
                request.phoneNumber()
        );

        Seller seller = manageSellerUseCase.updateSellerInfo(command);

        return ResponseEntity.ok(SellerResponse.from(seller));
    }

    /**
     * 창고 주소 수정
     */
    @PutMapping("/{sellerId}/warehouse-address")
    public ResponseEntity<SellerResponse> updateWarehouseAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String sellerId,
            @Valid @RequestBody WarehouseAddressRequest request
    ) {
        Seller seller = manageSellerUseCase.updateWarehouseAddress(sellerId, request.toCommand());

        return ResponseEntity.ok(SellerResponse.from(seller));
    }

    // ==================== Admin: 상태 관리 ====================

    /**
     * 판매자 승인
     */
    @PostMapping("/{sellerId}/approve")
    public ResponseEntity<SellerResponse> approveSeller(
            @PathVariable String sellerId
    ) {
        Seller seller = manageSellerUseCase.approveSeller(sellerId);

        return ResponseEntity.ok(SellerResponse.from(seller));
    }

    /**
     * 판매자 거절
     */
    @PostMapping("/{sellerId}/reject")
    public ResponseEntity<Void> rejectSeller(
            @PathVariable String sellerId,
            @Valid @RequestBody RejectSellerRequest request
    ) {
        manageSellerUseCase.rejectSeller(sellerId, request.reason());

        return ResponseEntity.noContent().build();
    }

    /**
     * 판매자 정지
     */
    @PostMapping("/{sellerId}/suspend")
    public ResponseEntity<SellerResponse> suspendSeller(
            @PathVariable String sellerId,
            @Valid @RequestBody SuspendSellerRequest request
    ) {
        Seller seller = manageSellerUseCase.suspendSeller(sellerId, request.reason());

        return ResponseEntity.ok(SellerResponse.from(seller));
    }

    /**
     * 판매자 활성화
     */
    @PostMapping("/{sellerId}/activate")
    public ResponseEntity<SellerResponse> activateSeller(
            @PathVariable String sellerId
    ) {
        Seller seller = manageSellerUseCase.activateSeller(sellerId);

        return ResponseEntity.ok(SellerResponse.from(seller));
    }

    /**
     * 판매자 휴면 전환
     */
    @PostMapping("/{sellerId}/dormant")
    public ResponseEntity<SellerResponse> makeDormant(
            @PathVariable String sellerId
    ) {
        Seller seller = manageSellerUseCase.makeDormant(sellerId);

        return ResponseEntity.ok(SellerResponse.from(seller));
    }

    /**
     * 판매자 폐업
     */
    @PostMapping("/{sellerId}/close")
    public ResponseEntity<SellerResponse> closeSeller(
            @PathVariable String sellerId
    ) {
        Seller seller = manageSellerUseCase.closeSeller(sellerId);

        return ResponseEntity.ok(SellerResponse.from(seller));
    }

    // ==================== 카테고리 관리 ====================

    /**
     * 카테고리 추가
     */
    @PostMapping("/{sellerId}/categories/{categoryId}")
    public ResponseEntity<SellerResponse> addCategory(
            @PathVariable String sellerId,
            @PathVariable String categoryId
    ) {
        Seller seller = manageSellerUseCase.addCategory(sellerId, categoryId);

        return ResponseEntity.ok(SellerResponse.from(seller));
    }

    /**
     * 카테고리 제거
     */
    @DeleteMapping("/{sellerId}/categories/{categoryId}")
    public ResponseEntity<SellerResponse> removeCategory(
            @PathVariable String sellerId,
            @PathVariable String categoryId
    ) {
        Seller seller = manageSellerUseCase.removeCategory(sellerId, categoryId);

        return ResponseEntity.ok(SellerResponse.from(seller));
    }

    // ==================== 조회 ====================

    /**
     * 판매자 조회
     */
    @GetMapping("/{sellerId}")
    public ResponseEntity<SellerResponse> getSeller(
            @PathVariable String sellerId
    ) {
        Seller seller = manageSellerUseCase.getSeller(sellerId);

        return ResponseEntity.ok(SellerResponse.from(seller));
    }

    /**
     * 사업자번호로 조회
     */
    @GetMapping("/business-number/{businessNumber}")
    public ResponseEntity<SellerResponse> getSellerByBusinessNumber(
            @PathVariable String businessNumber
    ) {
        Seller seller = manageSellerUseCase.getSellerByBusinessNumber(businessNumber);

        return ResponseEntity.ok(SellerResponse.from(seller));
    }

    /**
     * 전체 판매자 조회
     */
    @GetMapping
    public ResponseEntity<SellerListResponse> getAllSellers(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<Seller> sellers = manageSellerUseCase.getAllSellers(pageable);

        return ResponseEntity.ok(SellerListResponse.from(sellers));
    }

    /**
     * 상태별 판매자 조회
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<SellerListResponse> getSellersByStatus(
            @PathVariable SellerStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<Seller> sellers = manageSellerUseCase.getSellersByStatus(status, pageable);

        return ResponseEntity.ok(SellerListResponse.from(sellers));
    }

    /**
     * 승인 대기 판매자 조회
     */
    @GetMapping("/pending")
    public ResponseEntity<SellerListResponse> getPendingSellers(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<Seller> sellers = manageSellerUseCase.getPendingSellers(pageable);

        return ResponseEntity.ok(SellerListResponse.from(sellers));
    }
}
