package jjh.delivery.adapter.out.persistence.jpa;

import lombok.RequiredArgsConstructor;

import jjh.delivery.adapter.out.persistence.jpa.entity.CouponJpaEntity;
import jjh.delivery.adapter.out.persistence.jpa.mapper.CouponPersistenceMapper;
import jjh.delivery.adapter.out.persistence.jpa.repository.CouponJpaRepository;
import jjh.delivery.application.port.out.LoadCouponPort;
import jjh.delivery.application.port.out.SaveCouponPort;
import jjh.delivery.domain.promotion.Coupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Coupon JPA Adapter - Driven Adapter (Outbound)
 * JPA를 사용한 쿠폰 저장/조회 구현
 */
@Repository
@RequiredArgsConstructor
public class CouponJpaAdapter implements LoadCouponPort, SaveCouponPort {

    private final CouponJpaRepository couponJpaRepository;
    private final CouponPersistenceMapper couponMapper;

    // ==================== LoadCouponPort ====================

    @Override
    public Optional<Coupon> findById(String couponId) {
        return couponJpaRepository.findById(couponId)
                .map(couponMapper::toDomain);
    }

    @Override
    public Optional<Coupon> findByCode(String code) {
        return couponJpaRepository.findByCode(code)
                .map(couponMapper::toDomain);
    }

    @Override
    public Page<Coupon> findAll(Pageable pageable) {
        return couponJpaRepository.findAll(pageable)
                .map(couponMapper::toDomain);
    }

    @Override
    public Page<Coupon> findByActiveStatus(boolean isActive, Pageable pageable) {
        return couponJpaRepository.findByIsActive(isActive, pageable)
                .map(couponMapper::toDomain);
    }

    @Override
    public List<Coupon> findUsableCoupons() {
        // JPA 사용 (@Query with entity mapping)
        return couponJpaRepository.findUsableCoupons(LocalDateTime.now()).stream()
                .map(couponMapper::toDomain)
                .toList();
    }

    // ==================== SaveCouponPort ====================

    @Override
    public Coupon save(Coupon coupon) {
        CouponJpaEntity entity = couponMapper.toEntity(coupon);
        CouponJpaEntity savedEntity = couponJpaRepository.save(entity);
        return couponMapper.toDomain(savedEntity);
    }

    @Override
    public void delete(String couponId) {
        couponJpaRepository.deleteById(couponId);
    }
}
