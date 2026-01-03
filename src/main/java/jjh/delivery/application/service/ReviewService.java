package jjh.delivery.application.service;

import lombok.RequiredArgsConstructor;

import jjh.delivery.adapter.in.web.dto.CursorPageResponse;
import jjh.delivery.application.port.in.ManageReviewUseCase;
import jjh.delivery.application.port.out.LoadReviewPort;
import jjh.delivery.application.port.out.LoadReviewStatsPort;
import jjh.delivery.application.port.out.SaveReviewPort;
import jjh.delivery.domain.review.Review;
import jjh.delivery.domain.review.ReviewImage;
import jjh.delivery.domain.review.exception.ReviewNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

/**
 * Review Service - Application Layer
 */
@Service
@Transactional
@RequiredArgsConstructor
public class ReviewService implements ManageReviewUseCase {

    private final LoadReviewPort loadReviewPort;
    private final LoadReviewStatsPort loadReviewStatsPort;
    private final SaveReviewPort saveReviewPort;

    @Override
    public Review createReview(CreateReviewCommand command) {
        UUID orderId = UUID.fromString(command.orderId());
        UUID customerId = UUID.fromString(command.customerId());
        UUID sellerId = UUID.fromString(command.sellerId());
        UUID productId = UUID.fromString(command.productId());

        // 이미 해당 주문에 대한 리뷰가 있는지 확인
        if (loadReviewPort.existsByOrderId(orderId)) {
            throw new IllegalStateException("이미 해당 주문에 대한 리뷰가 존재합니다: " + command.orderId());
        }

        // Optional + IntStream으로 이미지 처리 (성능 개선: O(n²) → O(n))
        List<ReviewImage> images = Optional.ofNullable(command.imageUrls())
                .map(urls -> IntStream.range(0, urls.size())
                        .mapToObj(i -> ReviewImage.ofNew(UUID.randomUUID(), urls.get(i), i))
                        .toList())
                .orElse(List.of());

        Review review = Review.builder()
                .orderId(orderId)
                .customerId(customerId)
                .sellerId(sellerId)
                .productId(productId)
                .rating(command.rating())
                .content(command.content())
                .images(images)
                .build();

        return saveReviewPort.save(review);
    }

    @Override
    public Review updateReview(UpdateReviewCommand command) {
        UUID reviewId = UUID.fromString(command.reviewId());
        UUID customerId = UUID.fromString(command.customerId());

        Review review = loadReviewPort.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(command.reviewId()));

        // 리뷰 작성자만 수정 가능
        if (!review.getCustomerId().equals(customerId)) {
            throw new IllegalArgumentException("리뷰를 수정할 권한이 없습니다");
        }

        review.updateContent(command.rating(), command.content());

        // Optional + IntStream으로 이미지 교체 (함수형)
        Optional.ofNullable(command.imageUrls())
                .map(urls -> IntStream.range(0, urls.size())
                        .mapToObj(i -> ReviewImage.ofNew(UUID.randomUUID(), urls.get(i), i))
                        .toList())
                .ifPresent(review::replaceImages);

        return saveReviewPort.save(review);
    }

    @Override
    public void deleteReview(UUID reviewId, UUID customerId) {
        Review review = loadReviewPort.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId.toString()));

        // 리뷰 작성자만 삭제 가능
        if (!review.getCustomerId().equals(customerId)) {
            throw new IllegalArgumentException("리뷰를 삭제할 권한이 없습니다");
        }

        saveReviewPort.delete(reviewId);
    }

    @Override
    @Transactional(readOnly = true)
    public Review getReview(UUID reviewId) {
        return loadReviewPort.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId.toString()));
    }

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponse<Review> getReviewsByProductId(UUID productId, String cursor, int size) {
        return loadReviewPort.findByProductId(productId, cursor, size);
    }

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponse<Review> getMyReviews(UUID customerId, String cursor, int size) {
        return loadReviewPort.findByCustomerId(customerId, cursor, size);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewRatingInfo getProductRatingInfo(UUID productId) {
        double averageRating = loadReviewStatsPort.getAverageRatingByProductId(productId);
        long totalCount = loadReviewPort.countByProductId(productId);
        Map<Integer, Long> ratingDistribution = loadReviewStatsPort.getRatingDistributionByProductId(productId);

        return new ReviewRatingInfo(averageRating, totalCount, ratingDistribution);
    }

    @Override
    public Review addReply(UUID reviewId, UUID sellerId, String content) {
        Review review = loadReviewPort.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId.toString()));

        review.addReply(sellerId, content);

        return saveReviewPort.save(review);
    }

    @Override
    public Review updateReply(UUID reviewId, UUID sellerId, String content) {
        Review review = loadReviewPort.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId.toString()));

        review.updateReply(sellerId, content);

        return saveReviewPort.save(review);
    }

    @Override
    public Review deleteReply(UUID reviewId, UUID sellerId) {
        Review review = loadReviewPort.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId.toString()));

        review.deleteReply(sellerId);

        return saveReviewPort.save(review);
    }
}
