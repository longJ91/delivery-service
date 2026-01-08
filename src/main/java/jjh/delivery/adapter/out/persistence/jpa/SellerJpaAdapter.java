package jjh.delivery.adapter.out.persistence.jpa;

import lombok.RequiredArgsConstructor;

import jjh.delivery.adapter.in.web.dto.CursorPageResponse;
import jjh.delivery.adapter.in.web.dto.CursorValue;
import jjh.delivery.adapter.out.persistence.jpa.entity.SellerJpaEntity;
import jjh.delivery.adapter.out.persistence.jpa.mapper.SellerPersistenceMapper;
import jjh.delivery.adapter.out.persistence.jpa.repository.SellerJpaRepository;
import jjh.delivery.application.port.out.LoadSellerPort;
import jjh.delivery.application.port.out.SaveSellerPort;
import jjh.delivery.domain.seller.Seller;
import jjh.delivery.domain.seller.SellerStatus;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Seller JPA Adapter - Driven Adapter (Outbound)
 * JPA를 사용한 판매자 저장/조회 구현
 * Note: 단순 조회(findBusinessNameById)는 SellerJooqAdapter로 분리됨
 */
@Repository
@RequiredArgsConstructor
public class SellerJpaAdapter implements LoadSellerPort, SaveSellerPort {

    private final SellerJpaRepository repository;
    private final SellerPersistenceMapper mapper;

    // ==================== LoadSellerPort ====================

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(UUID sellerId) {
        return repository.existsById(sellerId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Seller> findById(UUID sellerId) {
        return repository.findById(sellerId)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Seller> findByBusinessNumber(String businessNumber) {
        return repository.findByBusinessNumber(businessNumber)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Seller> findByEmail(String email) {
        return repository.findByEmail(email)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponse<Seller> findAll(String cursor, int size) {
        CursorValue cursorValue = CursorValue.decode(cursor);

        List<SellerJpaEntity> entities;
        if (cursorValue != null) {
            LocalDateTime cursorCreatedAt = LocalDateTime.ofInstant(cursorValue.createdAt(), ZoneId.systemDefault());
            entities = repository.findAllWithCursor(cursorCreatedAt, cursorValue.id(), size + 1);
        } else {
            entities = repository.findAllOrderByCreatedAtDesc(size + 1);
        }

        List<Seller> sellers = entities.stream()
                .map(mapper::toDomain)
                .toList();

        return CursorPageResponse.ofWithUuidId(
                sellers,
                size,
                seller -> seller.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant(),
                Seller::getId
        );
    }

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponse<Seller> findByStatus(SellerStatus status, String cursor, int size) {
        CursorValue cursorValue = CursorValue.decode(cursor);

        List<SellerJpaEntity> entities;
        if (cursorValue != null) {
            LocalDateTime cursorCreatedAt = LocalDateTime.ofInstant(cursorValue.createdAt(), ZoneId.systemDefault());
            entities = repository.findByStatusWithCursor(status, cursorCreatedAt, cursorValue.id(), size + 1);
        } else {
            entities = repository.findByStatusOrderByCreatedAtDesc(status, size + 1);
        }

        List<Seller> sellers = entities.stream()
                .map(mapper::toDomain)
                .toList();

        return CursorPageResponse.ofWithUuidId(
                sellers,
                size,
                seller -> seller.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant(),
                Seller::getId
        );
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByBusinessNumber(String businessNumber) {
        return repository.existsByBusinessNumber(businessNumber);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    // ==================== SaveSellerPort ====================

    @Override
    @Transactional
    public Seller save(Seller seller) {
        SellerJpaEntity entity = mapper.toEntity(seller);
        SellerJpaEntity savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    @Transactional
    public void delete(UUID sellerId) {
        repository.deleteById(sellerId);
    }
}
