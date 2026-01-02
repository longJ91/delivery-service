package jjh.delivery.adapter.out.persistence.jpa;

import lombok.RequiredArgsConstructor;

import jjh.delivery.adapter.out.persistence.jpa.entity.SellerJpaEntity;
import jjh.delivery.adapter.out.persistence.jpa.mapper.SellerPersistenceMapper;
import jjh.delivery.adapter.out.persistence.jpa.repository.SellerJpaRepository;
import jjh.delivery.application.port.out.LoadSellerPort;
import jjh.delivery.application.port.out.SaveSellerPort;
import jjh.delivery.domain.seller.Seller;
import jjh.delivery.domain.seller.SellerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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
    public boolean existsById(String sellerId) {
        return repository.existsById(sellerId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Seller> findById(String sellerId) {
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
    public Page<Seller> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Seller> findByStatus(SellerStatus status, Pageable pageable) {
        return repository.findByStatus(status, pageable)
                .map(mapper::toDomain);
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
    public void delete(String sellerId) {
        repository.deleteById(sellerId);
    }
}
