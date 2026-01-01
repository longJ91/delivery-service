package jjh.delivery.application.service;

import jjh.delivery.application.port.in.ManageSellerUseCase;
import jjh.delivery.application.port.out.LoadSellerPort;
import jjh.delivery.application.port.out.SaveSellerPort;
import jjh.delivery.domain.seller.Seller;
import jjh.delivery.domain.seller.SellerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.NoSuchElementException;

/**
 * Seller Service - Application Service
 * 판매자 관리 서비스
 */
@Service
@Transactional
public class SellerService implements ManageSellerUseCase {

    private final LoadSellerPort loadSellerPort;
    private final SaveSellerPort saveSellerPort;

    public SellerService(LoadSellerPort loadSellerPort, SaveSellerPort saveSellerPort) {
        this.loadSellerPort = loadSellerPort;
        this.saveSellerPort = saveSellerPort;
    }

    // ==================== 판매자 등록/수정 ====================

    @Override
    public Seller registerSeller(RegisterSellerCommand command) {
        // 중복 체크
        if (loadSellerPort.existsByBusinessNumber(command.businessNumber())) {
            throw new IllegalArgumentException("Business number already registered: " + command.businessNumber());
        }
        if (loadSellerPort.existsByEmail(command.email())) {
            throw new IllegalArgumentException("Email already registered: " + command.email());
        }

        Seller.Builder builder = Seller.builder()
                .businessName(command.businessName())
                .businessNumber(command.businessNumber())
                .representativeName(command.representativeName())
                .email(command.email())
                .phoneNumber(command.phoneNumber())
                .sellerType(command.sellerType())
                .status(SellerStatus.PENDING);

        if (command.warehouseAddress() != null) {
            builder.warehouseAddress(command.warehouseAddress().toDomain());
        }

        if (command.categoryIds() != null) {
            builder.categoryIds(new ArrayList<>(command.categoryIds()));
        }

        Seller seller = builder.build();
        return saveSellerPort.save(seller);
    }

    @Override
    public Seller updateSellerInfo(UpdateSellerInfoCommand command) {
        Seller seller = getSeller(command.sellerId());

        seller.updateInfo(
                command.businessName() != null ? command.businessName() : seller.getBusinessName(),
                command.representativeName() != null ? command.representativeName() : seller.getRepresentativeName(),
                command.email() != null ? command.email() : seller.getEmail(),
                command.phoneNumber() != null ? command.phoneNumber() : seller.getPhoneNumber()
        );

        return saveSellerPort.save(seller);
    }

    @Override
    public Seller updateWarehouseAddress(String sellerId, WarehouseAddressCommand warehouseAddress) {
        Seller seller = getSeller(sellerId);
        seller.updateWarehouseAddress(warehouseAddress.toDomain());
        return saveSellerPort.save(seller);
    }

    // ==================== 판매자 상태 관리 ====================

    @Override
    public Seller approveSeller(String sellerId) {
        Seller seller = getSeller(sellerId);
        seller.approve();
        return saveSellerPort.save(seller);
    }

    @Override
    public void rejectSeller(String sellerId, String reason) {
        Seller seller = getSeller(sellerId);
        if (seller.getStatus() != SellerStatus.PENDING) {
            throw new IllegalStateException("Only pending sellers can be rejected");
        }
        // 거절 시 삭제 또는 상태 변경
        saveSellerPort.delete(sellerId);
    }

    @Override
    public Seller suspendSeller(String sellerId, String reason) {
        Seller seller = getSeller(sellerId);
        seller.suspend();
        return saveSellerPort.save(seller);
    }

    @Override
    public Seller activateSeller(String sellerId) {
        Seller seller = getSeller(sellerId);
        seller.activate();
        return saveSellerPort.save(seller);
    }

    @Override
    public Seller makeDormant(String sellerId) {
        Seller seller = getSeller(sellerId);
        seller.makeDormant();
        return saveSellerPort.save(seller);
    }

    @Override
    public Seller closeSeller(String sellerId) {
        Seller seller = getSeller(sellerId);
        seller.close();
        return saveSellerPort.save(seller);
    }

    // ==================== 카테고리 관리 ====================

    @Override
    public Seller addCategory(String sellerId, String categoryId) {
        Seller seller = getSeller(sellerId);
        seller.addCategory(categoryId);
        return saveSellerPort.save(seller);
    }

    @Override
    public Seller removeCategory(String sellerId, String categoryId) {
        Seller seller = getSeller(sellerId);
        seller.removeCategory(categoryId);
        return saveSellerPort.save(seller);
    }

    // ==================== 조회 ====================

    @Override
    @Transactional(readOnly = true)
    public Seller getSeller(String sellerId) {
        return loadSellerPort.findById(sellerId)
                .orElseThrow(() -> new NoSuchElementException("Seller not found: " + sellerId));
    }

    @Override
    @Transactional(readOnly = true)
    public Seller getSellerByBusinessNumber(String businessNumber) {
        return loadSellerPort.findByBusinessNumber(businessNumber)
                .orElseThrow(() -> new NoSuchElementException("Seller not found with business number: " + businessNumber));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Seller> getAllSellers(Pageable pageable) {
        return loadSellerPort.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Seller> getSellersByStatus(SellerStatus status, Pageable pageable) {
        return loadSellerPort.findByStatus(status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Seller> getPendingSellers(Pageable pageable) {
        return loadSellerPort.findByStatus(SellerStatus.PENDING, pageable);
    }
}
