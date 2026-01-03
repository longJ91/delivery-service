package jjh.delivery.adapter.out.persistence.jpa;

import lombok.RequiredArgsConstructor;

import jjh.delivery.adapter.out.persistence.jpa.entity.ReviewJpaEntity;
import jjh.delivery.adapter.out.persistence.jpa.mapper.ReviewPersistenceMapper;
import jjh.delivery.adapter.out.persistence.jpa.repository.ReviewJpaRepository;
import jjh.delivery.application.port.out.LoadReviewPort;
import jjh.delivery.application.port.out.SaveReviewPort;
import jjh.delivery.domain.review.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Review JPA Adapter - Driven Adapter (Outbound)
 * JPA를 사용한 리뷰 저장/조회 구현
 * Note: 통계 쿼리(getAverageRating, getRatingDistribution)는 ReviewJooqAdapter로 분리됨
 */
@Component
@RequiredArgsConstructor
public class ReviewJpaAdapter implements LoadReviewPort, SaveReviewPort {

    private final ReviewJpaRepository repository;
    private final ReviewPersistenceMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<Review> findById(UUID reviewId) {
        return repository.findByIdWithDetails(reviewId)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Review> findByProductId(UUID productId, Pageable pageable) {
        return repository.findByProductIdAndIsVisibleTrue(productId, pageable)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByProductId(UUID productId) {
        return repository.countByProductIdAndIsVisibleTrue(productId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Review> findByCustomerId(UUID customerId, Pageable pageable) {
        return repository.findByCustomerId(customerId, pageable)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Review> findBySellerId(UUID sellerId, Pageable pageable) {
        return repository.findBySellerId(sellerId, pageable)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByOrderId(UUID orderId) {
        return repository.existsByOrderId(orderId);
    }

    @Override
    public Review save(Review review) {
        ReviewJpaEntity entity = mapper.toEntity(review);
        ReviewJpaEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public void delete(UUID reviewId) {
        repository.deleteById(reviewId);
    }
}
