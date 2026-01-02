package jjh.delivery.adapter.in.web.review;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import jjh.delivery.adapter.in.web.review.dto.*;
import jjh.delivery.application.port.in.ManageReviewUseCase;
import jjh.delivery.application.port.in.ManageReviewUseCase.*;
import jjh.delivery.domain.review.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * Review REST Controller - Driving Adapter (Inbound)
 * 리뷰 관리 API
 */
@RestController
@RequestMapping("/api/v2/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ManageReviewUseCase manageReviewUseCase;

    /**
     * 리뷰 작성
     */
    @PostMapping
    public ResponseEntity<ReviewDetailResponse> createReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateReviewRequest request
    ) {
        String customerId = userDetails.getUsername();

        CreateReviewCommand command = new CreateReviewCommand(
                customerId,
                request.orderId(),
                request.sellerId(),
                request.productId(),
                request.rating(),
                request.content(),
                request.imageUrls()
        );

        Review review = manageReviewUseCase.createReview(command);

        return ResponseEntity.status(HttpStatus.CREATED).body(ReviewDetailResponse.from(review));
    }

    /**
     * 리뷰 조회
     */
    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewDetailResponse> getReview(
            @PathVariable String reviewId
    ) {
        Review review = manageReviewUseCase.getReview(reviewId);

        return ResponseEntity.ok(ReviewDetailResponse.from(review));
    }

    /**
     * 리뷰 수정
     */
    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewDetailResponse> updateReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String reviewId,
            @Valid @RequestBody UpdateReviewRequest request
    ) {
        String customerId = userDetails.getUsername();

        UpdateReviewCommand command = new UpdateReviewCommand(
                reviewId,
                customerId,
                request.rating(),
                request.content(),
                request.imageUrls()
        );

        Review review = manageReviewUseCase.updateReview(command);

        return ResponseEntity.ok(ReviewDetailResponse.from(review));
    }

    /**
     * 리뷰 삭제
     */
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String reviewId
    ) {
        String customerId = userDetails.getUsername();
        manageReviewUseCase.deleteReview(reviewId, customerId);

        return ResponseEntity.noContent().build();
    }

    /**
     * 내 리뷰 목록 조회
     */
    @GetMapping("/my")
    public ResponseEntity<ReviewListResponse> getMyReviews(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        String customerId = userDetails.getUsername();
        Page<Review> reviews = manageReviewUseCase.getMyReviews(customerId, pageable);

        return ResponseEntity.ok(ReviewListResponse.from(reviews));
    }

    /**
     * 상품별 리뷰 목록 조회
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<ReviewListResponse> getReviewsByProductId(
            @PathVariable String productId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<Review> reviews = manageReviewUseCase.getReviewsByProductId(productId, pageable);

        return ResponseEntity.ok(ReviewListResponse.from(reviews));
    }

    /**
     * 상품별 평점 정보 조회
     */
    @GetMapping("/product/{productId}/rating")
    public ResponseEntity<ReviewRatingInfoResponse> getProductRatingInfo(
            @PathVariable String productId
    ) {
        ReviewRatingInfo ratingInfo = manageReviewUseCase.getProductRatingInfo(productId);

        return ResponseEntity.ok(ReviewRatingInfoResponse.from(ratingInfo));
    }

    // ==================== Seller Reply Endpoints ====================

    /**
     * 판매자 답글 추가
     */
    @PostMapping("/{reviewId}/reply")
    public ResponseEntity<ReviewDetailResponse> addReply(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String reviewId,
            @Valid @RequestBody AddReplyRequest request
    ) {
        String sellerId = userDetails.getUsername();
        Review review = manageReviewUseCase.addReply(reviewId, sellerId, request.content());

        return ResponseEntity.status(HttpStatus.CREATED).body(ReviewDetailResponse.from(review));
    }

    /**
     * 판매자 답글 수정
     */
    @PutMapping("/{reviewId}/reply")
    public ResponseEntity<ReviewDetailResponse> updateReply(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String reviewId,
            @Valid @RequestBody AddReplyRequest request
    ) {
        String sellerId = userDetails.getUsername();
        Review review = manageReviewUseCase.updateReply(reviewId, sellerId, request.content());

        return ResponseEntity.ok(ReviewDetailResponse.from(review));
    }

    /**
     * 판매자 답글 삭제
     */
    @DeleteMapping("/{reviewId}/reply")
    public ResponseEntity<ReviewDetailResponse> deleteReply(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String reviewId
    ) {
        String sellerId = userDetails.getUsername();
        Review review = manageReviewUseCase.deleteReply(reviewId, sellerId);

        return ResponseEntity.ok(ReviewDetailResponse.from(review));
    }
}
