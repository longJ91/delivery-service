package jjh.delivery.application.port.in;

import jjh.delivery.domain.seller.Seller;
import jjh.delivery.domain.seller.SellerStatus;
import jjh.delivery.domain.seller.SellerType;
import jjh.delivery.domain.seller.WarehouseAddress;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

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
    Seller updateWarehouseAddress(String sellerId, WarehouseAddressCommand warehouseAddress);

    // ==================== 판매자 상태 관리 (Admin) ====================

    /**
     * 판매자 승인
     */
    Seller approveSeller(String sellerId);

    /**
     * 판매자 거절
     */
    void rejectSeller(String sellerId, String reason);

    /**
     * 판매자 정지
     */
    Seller suspendSeller(String sellerId, String reason);

    /**
     * 판매자 활성화
     */
    Seller activateSeller(String sellerId);

    /**
     * 판매자 휴면 전환
     */
    Seller makeDormant(String sellerId);

    /**
     * 판매자 폐업
     */
    Seller closeSeller(String sellerId);

    // ==================== 카테고리 관리 ====================

    /**
     * 카테고리 추가
     */
    Seller addCategory(String sellerId, String categoryId);

    /**
     * 카테고리 제거
     */
    Seller removeCategory(String sellerId, String categoryId);

    // ==================== 조회 ====================

    /**
     * 판매자 조회
     */
    Seller getSeller(String sellerId);

    /**
     * 사업자번호로 조회
     */
    Seller getSellerByBusinessNumber(String businessNumber);

    /**
     * 전체 판매자 조회
     */
    Page<Seller> getAllSellers(Pageable pageable);

    /**
     * 상태별 판매자 조회
     */
    Page<Seller> getSellersByStatus(SellerStatus status, Pageable pageable);

    /**
     * 승인 대기 판매자 조회
     */
    Page<Seller> getPendingSellers(Pageable pageable);

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
