package jjh.delivery.adapter.out.persistence.jpa;

import lombok.RequiredArgsConstructor;

import jjh.delivery.adapter.in.web.dto.CursorPageResponse;
import jjh.delivery.adapter.in.web.dto.CursorValue;
import jjh.delivery.adapter.out.persistence.jpa.entity.CouponJpaEntity;
import jjh.delivery.adapter.out.persistence.jpa.mapper.CouponPersistenceMapper;
import jjh.delivery.adapter.out.persistence.jpa.repository.CouponJpaRepository;
import jjh.delivery.application.port.out.LoadCouponPort;
import jjh.delivery.application.port.out.SaveCouponPort;
import jjh.delivery.domain.promotion.Coupon;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Coupon JPA Adapter - Driven Adapter (Outbound)
 * JPA를 사용한 쿠폰 저장/조회 구현 (커서 기반 페이지네이션)
 */
@Repository
@RequiredArgsConstructor
public class CouponJpaAdapter implements LoadCouponPort, SaveCouponPort {

    private final CouponJpaRepository couponJpaRepository;
    private final CouponPersistenceMapper couponMapper;

    // ==================== LoadCouponPort ====================

    @Override
    public Optional<Coupon> findById(UUID couponId) {
        return couponJpaRepository.findById(couponId)
                .map(couponMapper::toDomain);
    }

    @Override
    public Optional<Coupon> findByCode(String code) {
        return couponJpaRepository.findByCode(code)
                .map(couponMapper::toDomain);
    }

    @Override
    public CursorPageResponse<Coupon> findAll(String cursor, int size) {
        CursorValue cursorValue = CursorValue.decode(cursor);

        List<CouponJpaEntity> entities;
        if (cursorValue != null) {
            LocalDateTime cursorCreatedAt = LocalDateTime.ofInstant(cursorValue.createdAt(), ZoneId.systemDefault());
            entities = couponJpaRepository.findAllWithCursor(cursorCreatedAt, cursorValue.id(), size + 1);
        } else {
            entities = couponJpaRepository.findAllOrderByCreatedAtDesc(size + 1);
        }

        List<Coupon> coupons = entities.stream()
                .map(couponMapper::toDomain)
                .toList();

        return CursorPageResponse.of(
                coupons,
                size,
                coupon -> coupon.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant(),
                Coupon::getId
        );
    }

    @Override
    public CursorPageResponse<Coupon> findByActiveStatus(boolean isActive, String cursor, int size) {
        CursorValue cursorValue = CursorValue.decode(cursor);

        List<CouponJpaEntity> entities;
        if (cursorValue != null) {
            LocalDateTime cursorCreatedAt = LocalDateTime.ofInstant(cursorValue.createdAt(), ZoneId.systemDefault());
            entities = couponJpaRepository.findByIsActiveWithCursor(isActive, cursorCreatedAt, cursorValue.id(), size + 1);
        } else {
            entities = couponJpaRepository.findByIsActiveOrderByCreatedAtDesc(isActive, size + 1);
        }

        List<Coupon> coupons = entities.stream()
                .map(couponMapper::toDomain)
                .toList();

        return CursorPageResponse.of(
                coupons,
                size,
                coupon -> coupon.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant(),
                Coupon::getId
        );
    }

    @Override
    public List<Coupon> findUsableCoupons() {
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
    public void delete(UUID couponId) {
        couponJpaRepository.deleteById(couponId);
    }
}
