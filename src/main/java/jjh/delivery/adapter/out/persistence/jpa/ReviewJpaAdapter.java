package jjh.delivery.adapter.out.persistence.jpa;

import lombok.RequiredArgsConstructor;

import jjh.delivery.adapter.in.web.dto.CursorPageResponse;
import jjh.delivery.adapter.in.web.dto.CursorValue;
import jjh.delivery.adapter.out.persistence.jpa.entity.ReviewJpaEntity;
import jjh.delivery.adapter.out.persistence.jpa.mapper.ReviewPersistenceMapper;
import jjh.delivery.adapter.out.persistence.jpa.repository.ReviewJpaRepository;
import jjh.delivery.application.port.out.LoadReviewPort;
import jjh.delivery.application.port.out.SaveReviewPort;
import jjh.delivery.domain.review.Review;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Review JPA Adapter - Driven Adapter (Outbound)
 * JPA를 사용한 리뷰 저장/조회 구현 (커서 기반 페이지네이션)
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
    public CursorPageResponse<Review> findByProductId(UUID productId, String cursor, int size) {
        CursorValue cursorValue = CursorValue.decode(cursor);

        List<ReviewJpaEntity> entities;
        if (cursorValue != null) {
            LocalDateTime cursorCreatedAt = LocalDateTime.ofInstant(cursorValue.createdAt(), ZoneId.systemDefault());
            entities = repository.findByProductIdWithCursor(productId, cursorCreatedAt, cursorValue.id(), size + 1);
        } else {
            entities = repository.findByProductIdOrderByCreatedAtDesc(productId, size + 1);
        }

        List<Review> reviews = entities.stream()
                .map(mapper::toDomain)
                .toList();

        return CursorPageResponse.ofWithUuidId(
                reviews,
                size,
                review -> review.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant(),
                Review::getId
        );
    }

    @Override
    @Transactional(readOnly = true)
    public long countByProductId(UUID productId) {
        return repository.countByProductIdAndIsVisibleTrue(productId);
    }

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponse<Review> findByCustomerId(UUID customerId, String cursor, int size) {
        CursorValue cursorValue = CursorValue.decode(cursor);

        List<ReviewJpaEntity> entities;
        if (cursorValue != null) {
            LocalDateTime cursorCreatedAt = LocalDateTime.ofInstant(cursorValue.createdAt(), ZoneId.systemDefault());
            entities = repository.findByCustomerIdWithCursor(customerId, cursorCreatedAt, cursorValue.id(), size + 1);
        } else {
            entities = repository.findByCustomerIdOrderByCreatedAtDesc(customerId, size + 1);
        }

        List<Review> reviews = entities.stream()
                .map(mapper::toDomain)
                .toList();

        return CursorPageResponse.ofWithUuidId(
                reviews,
                size,
                review -> review.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant(),
                Review::getId
        );
    }

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponse<Review> findBySellerId(UUID sellerId, String cursor, int size) {
        CursorValue cursorValue = CursorValue.decode(cursor);

        List<ReviewJpaEntity> entities;
        if (cursorValue != null) {
            LocalDateTime cursorCreatedAt = LocalDateTime.ofInstant(cursorValue.createdAt(), ZoneId.systemDefault());
            entities = repository.findBySellerIdWithCursor(sellerId, cursorCreatedAt, cursorValue.id(), size + 1);
        } else {
            entities = repository.findBySellerIdOrderByCreatedAtDesc(sellerId, size + 1);
        }

        List<Review> reviews = entities.stream()
                .map(mapper::toDomain)
                .toList();

        return CursorPageResponse.ofWithUuidId(
                reviews,
                size,
                review -> review.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant(),
                Review::getId
        );
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
