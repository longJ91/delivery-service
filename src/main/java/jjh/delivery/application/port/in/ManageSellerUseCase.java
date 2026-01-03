package jjh.delivery.application.port.in;

import jjh.delivery.adapter.in.web.dto.CursorPageResponse;
import jjh.delivery.domain.seller.Seller;
import jjh.delivery.domain.seller.SellerStatus;
import jjh.delivery.domain.seller.SellerType;
import jjh.delivery.domain.seller.WarehouseAddress;

import java.util.List;
import java.util.UUID;

/**
 * Manage Seller Use Case - Driving Port (Inbound)
 * 판매자 관리 유스케이스
 */
public interface ManageSellerUseCase {

    // ==================== 판매자 등록/수정 ====================

    /**
     * 판매자 등록 신청
     */
    Seller registerSeller(RegisterSellerCommand command);

    /**
     * 판매자 정보 수정
     */
    Seller updateSellerInfo(UpdateSellerInfoCommand command);

    /**
     * 창고 주소 수정
     */
    Seller updateWarehouseAddress(UUID sellerId, WarehouseAddressCommand warehouseAddress);

    // ==================== 판매자 상태 관리 (Admin) ====================

    /**
     * 판매자 승인
     */
    Seller approveSeller(UUID sellerId);

    /**
     * 판매자 거절
     */
    void rejectSeller(UUID sellerId, String reason);

    /**
     * 판매자 정지
     */
    Seller suspendSeller(UUID sellerId, String reason);

    /**
     * 판매자 활성화
     */
    Seller activateSeller(UUID sellerId);

    /**
     * 판매자 휴면 전환
     */
    Seller makeDormant(UUID sellerId);

    /**
     * 판매자 폐업
     */
    Seller closeSeller(UUID sellerId);

    // ==================== 카테고리 관리 ====================

    /**
     * 카테고리 추가
     */
    Seller addCategory(UUID sellerId, UUID categoryId);

    /**
     * 카테고리 제거
     */
    Seller removeCategory(UUID sellerId, UUID categoryId);

    // ==================== 조회 ====================

    /**
     * 판매자 조회
     */
    Seller getSeller(UUID sellerId);

    /**
     * 사업자번호로 조회
     */
    Seller getSellerByBusinessNumber(String businessNumber);

    /**
     * 전체 판매자 조회 (커서 기반 페이지네이션)
     */
    CursorPageResponse<Seller> getAllSellers(String cursor, int size);

    /**
     * 상태별 판매자 조회 (커서 기반 페이지네이션)
     */
    CursorPageResponse<Seller> getSellersByStatus(SellerStatus status, String cursor, int size);

    /**
     * 승인 대기 판매자 조회 (커서 기반 페이지네이션)
     */
    CursorPageResponse<Seller> getPendingSellers(String cursor, int size);

    // ==================== Commands ====================

    record RegisterSellerCommand(
            String businessName,
            String businessNumber,
            String representativeName,
            String email,
            String phoneNumber,
            SellerType sellerType,
            WarehouseAddressCommand warehouseAddress,
            List<String> categoryIds
    ) {
        public RegisterSellerCommand {
            if (businessName == null || businessName.isBlank()) {
                throw new IllegalArgumentException("Business name is required");
            }
            if (businessNumber == null || businessNumber.isBlank()) {
                throw new IllegalArgumentException("Business number is required");
            }
            if (representativeName == null || representativeName.isBlank()) {
                throw new IllegalArgumentException("Representative name is required");
            }
            if (email == null || email.isBlank()) {
                throw new IllegalArgumentException("Email is required");
            }
        }
    }

    record UpdateSellerInfoCommand(
            String sellerId,
            String businessName,
            String representativeName,
            String email,
            String phoneNumber
    ) {
        public UpdateSellerInfoCommand {
            if (sellerId == null || sellerId.isBlank()) {
                throw new IllegalArgumentException("Seller ID is required");
            }
        }
    }

    record WarehouseAddressCommand(
            String postalCode,
            String address1,
            String address2,
            String contactName,
            String contactPhone
    ) {
        public WarehouseAddress toDomain() {
            return WarehouseAddress.of(postalCode, address1, address2, contactName, contactPhone);
        }
    }
}
