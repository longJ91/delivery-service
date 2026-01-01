package jjh.delivery.application.service;

import jjh.delivery.application.port.in.ManageReviewUseCase;
import jjh.delivery.application.port.out.LoadReviewPort;
import jjh.delivery.application.port.out.SaveReviewPort;
import jjh.delivery.domain.review.Review;
import jjh.delivery.domain.review.ReviewImage;
import jjh.delivery.domain.review.exception.ReviewNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Review Service - Application Layer
 */
@Service
@Transactional
public class ReviewService implements ManageReviewUseCase {

    private final LoadReviewPort loadReviewPort;
    private final SaveReviewPort saveReviewPort;

    public ReviewService(
            LoadReviewPort loadReviewPort,
            SaveReviewPort saveReviewPort
    ) {
        this.loadReviewPort = loadReviewPort;
        this.saveReviewPort = saveReviewPort;
    }

    @Override
    public Review createReview(CreateReviewCommand command) {
        // 이미 해당 주문에 대한 리뷰가 있는지 확인
        if (loadReviewPort.existsByOrderId(command.orderId())) {
            throw new IllegalStateException("이미 해당 주문에 대한 리뷰가 존재합니다: " + command.orderId());
        }

        List<ReviewImage> images = command.imageUrls() != null ?
                command.imageUrls().stream()
                        .map(url -> ReviewImage.ofNew(url, command.imageUrls().indexOf(url)))
                        .toList()
                : List.of();

        Review review = Review.builder()
                .orderId(command.orderId())
                .customerId(command.customerId())
                .sellerId(command.sellerId())
                .productId(command.productId())
                .rating(command.rating())
                .content(command.content())
                .images(images)
                .build();

        return saveReviewPort.save(review);
    }

    @Override
    public Review updateReview(UpdateReviewCommand command) {
        Review review = loadReviewPort.findById(command.reviewId())
                .orElseThrow(() -> new ReviewNotFoundException(command.reviewId()));

        // 리뷰 작성자만 수정 가능
        if (!review.getCustomerId().equals(command.customerId())) {
            throw new IllegalArgumentException("리뷰를 수정할 권한이 없습니다");
        }

        review.updateContent(command.rating(), command.content());

        // 이미지 교체
        if (command.imageUrls() != null) {
            List<ReviewImage> images = command.imageUrls().stream()
                    .map(url -> ReviewImage.ofNew(url, command.imageUrls().indexOf(url)))
                    .toList();
            review.replaceImages(images);
        }

        return saveReviewPort.save(review);
    }

    @Override
    public void deleteReview(String reviewId, String customerId) {
        Review review = loadReviewPort.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId));

        // 리뷰 작성자만 삭제 가능
        if (!review.getCustomerId().equals(customerId)) {
            throw new IllegalArgumentException("리뷰를 삭제할 권한이 없습니다");
        }

        saveReviewPort.delete(reviewId);
    }

    @Override
    @Transactional(readOnly = true)
    public Review getReview(String reviewId) {
        return loadReviewPort.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Review> getReviewsByProductId(String productId, Pageable pageable) {
        return loadReviewPort.findByProductId(productId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Review> getMyReviews(String customerId, Pageable pageable) {
        return loadReviewPort.findByCustomerId(customerId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewRatingInfo getProductRatingInfo(String productId) {
        double averageRating = loadReviewPort.getAverageRatingByProductId(productId);
        long totalCount = loadReviewPort.countByProductId(productId);
        Map<Integer, Long> ratingDistribution = loadReviewPort.getRatingDistributionByProductId(productId);

        return new ReviewRatingInfo(averageRating, totalCount, ratingDistribution);
    }

    @Override
    public Review addReply(String reviewId, String sellerId, String content) {
        Review review = loadReviewPort.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId));

        review.addReply(sellerId, content);

        return saveReviewPort.save(review);
    }

    @Override
    public Review updateReply(String reviewId, String sellerId, String content) {
        Review review = loadReviewPort.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId));

        review.updateReply(sellerId, content);

        return saveReviewPort.save(review);
    }

    @Override
    public Review deleteReply(String reviewId, String sellerId) {
        Review review = loadReviewPort.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId));

        review.deleteReply(sellerId);

        return saveReviewPort.save(review);
    }
}
