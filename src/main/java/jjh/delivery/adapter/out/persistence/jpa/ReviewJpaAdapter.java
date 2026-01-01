package jjh.delivery.adapter.out.persistence.jpa;

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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Review JPA Adapter - Driven Adapter (Outbound)
 */
@Component
public class ReviewJpaAdapter implements LoadReviewPort, SaveReviewPort {

    private final ReviewJpaRepository repository;
    private final ReviewPersistenceMapper mapper;

    public ReviewJpaAdapter(ReviewJpaRepository repository, ReviewPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Review> findById(String reviewId) {
        return repository.findByIdWithDetails(reviewId)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Review> findByProductId(String productId, Pageable pageable) {
        return repository.findByProductIdAndIsVisibleTrue(productId, pageable)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public double getAverageRatingByProductId(String productId) {
        return repository.getAverageRatingByProductId(productId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByProductId(String productId) {
        return repository.countByProductIdAndIsVisibleTrue(productId);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Integer, Long> getRatingDistributionByProductId(String productId) {
        List<Object[]> results = repository.getRatingDistributionByProductId(productId);
        Map<Integer, Long> distribution = new HashMap<>();

        // 기본값 설정
        for (int i = 1; i <= 5; i++) {
            distribution.put(i, 0L);
        }

        // 실제 값으로 업데이트
        for (Object[] row : results) {
            Integer rating = (Integer) row[0];
            Long count = (Long) row[1];
            distribution.put(rating, count);
        }

        return distribution;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Review> findByCustomerId(String customerId, Pageable pageable) {
        return repository.findByCustomerId(customerId, pageable)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Review> findBySellerId(String sellerId, Pageable pageable) {
        return repository.findBySellerId(sellerId, pageable)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByOrderId(String orderId) {
        return repository.existsByOrderId(orderId);
    }

    @Override
    public Review save(Review review) {
        ReviewJpaEntity entity = mapper.toEntity(review);
        ReviewJpaEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public void delete(String reviewId) {
        repository.deleteById(reviewId);
    }
}
