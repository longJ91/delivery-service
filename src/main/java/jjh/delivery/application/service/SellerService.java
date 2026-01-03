package jjh.delivery.application.service;

import lombok.RequiredArgsConstructor;

import jjh.delivery.adapter.in.web.dto.CursorPageResponse;
import jjh.delivery.application.port.in.ManageSellerUseCase;
import jjh.delivery.application.port.out.LoadSellerPort;
import jjh.delivery.application.port.out.SaveSellerPort;
import jjh.delivery.domain.seller.Seller;
import jjh.delivery.domain.seller.SellerStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Seller Service - Application Service
 * 판매자 관리 서비스
 */
@Service
@Transactional
@RequiredArgsConstructor
public class SellerService implements ManageSellerUseCase {

    private final LoadSellerPort loadSellerPort;
    private final SaveSellerPort saveSellerPort;

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

        // Optional.ifPresent로 조건부 설정 (함수형)
        Optional.ofNullable(command.warehouseAddress())
                .map(ManageSellerUseCase.WarehouseAddressCommand::toDomain)
                .ifPresent(builder::warehouseAddress);

        Optional.ofNullable(command.categoryIds())
                .map(ids -> ids.stream().map(UUID::fromString).toList())
                .ifPresent(builder::categoryIds);

        Seller seller = builder.build();
        return saveSellerPort.save(seller);
    }

    @Override
    public Seller updateSellerInfo(UpdateSellerInfoCommand command) {
        Seller seller = getSeller(UUID.fromString(command.sellerId()));

        // Optional.orElseGet으로 조건부 값 결정 (함수형)
        seller.updateInfo(
                getOrDefault(command.businessName(), seller::getBusinessName),
                getOrDefault(command.representativeName(), seller::getRepresentativeName),
                getOrDefault(command.email(), seller::getEmail),
                getOrDefault(command.phoneNumber(), seller::getPhoneNumber)
        );

        return saveSellerPort.save(seller);
    }

    /**
     * null이 아니면 새 값을 반환하고, null이면 기본값 공급자에서 값을 가져옴
     */
    private <T> T getOrDefault(T value, Supplier<T> defaultSupplier) {
        return Optional.ofNullable(value).orElseGet(defaultSupplier);
    }

    @Override
    public Seller updateWarehouseAddress(UUID sellerId, WarehouseAddressCommand warehouseAddress) {
        Seller seller = getSeller(sellerId);
        seller.updateWarehouseAddress(warehouseAddress.toDomain());
        return saveSellerPort.save(seller);
    }

    // ==================== 판매자 상태 관리 ====================

    @Override
    public Seller approveSeller(UUID sellerId) {
        Seller seller = getSeller(sellerId);
        seller.approve();
        return saveSellerPort.save(seller);
    }

    @Override
    public void rejectSeller(UUID sellerId, String reason) {
        Seller seller = getSeller(sellerId);
        if (seller.getStatus() != SellerStatus.PENDING) {
            throw new IllegalStateException("Only pending sellers can be rejected");
        }
        // 거절 시 삭제 또는 상태 변경
        saveSellerPort.delete(sellerId);
    }

    @Override
    public Seller suspendSeller(UUID sellerId, String reason) {
        Seller seller = getSeller(sellerId);
        seller.suspend();
        return saveSellerPort.save(seller);
    }

    @Override
    public Seller activateSeller(UUID sellerId) {
        Seller seller = getSeller(sellerId);
        seller.activate();
        return saveSellerPort.save(seller);
    }

    @Override
    public Seller makeDormant(UUID sellerId) {
        Seller seller = getSeller(sellerId);
        seller.makeDormant();
        return saveSellerPort.save(seller);
    }

    @Override
    public Seller closeSeller(UUID sellerId) {
        Seller seller = getSeller(sellerId);
        seller.close();
        return saveSellerPort.save(seller);
    }

    // ==================== 카테고리 관리 ====================

    @Override
    public Seller addCategory(UUID sellerId, UUID categoryId) {
        Seller seller = getSeller(sellerId);
        seller.addCategory(categoryId);
        return saveSellerPort.save(seller);
    }

    @Override
    public Seller removeCategory(UUID sellerId, UUID categoryId) {
        Seller seller = getSeller(sellerId);
        seller.removeCategory(categoryId);
        return saveSellerPort.save(seller);
    }

    // ==================== 조회 ====================

    @Override
    @Transactional(readOnly = true)
    public Seller getSeller(UUID sellerId) {
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
    public CursorPageResponse<Seller> getAllSellers(String cursor, int size) {
        return loadSellerPort.findAll(cursor, size);
    }

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponse<Seller> getSellersByStatus(SellerStatus status, String cursor, int size) {
        return loadSellerPort.findByStatus(status, cursor, size);
    }

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponse<Seller> getPendingSellers(String cursor, int size) {
        return loadSellerPort.findByStatus(SellerStatus.PENDING, cursor, size);
    }
}
