package jjh.delivery.adapter.in.web.review;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import jjh.delivery.adapter.in.web.dto.CursorPageResponse;
import jjh.delivery.adapter.in.web.review.dto.*;
import jjh.delivery.application.port.in.ManageReviewUseCase;
import jjh.delivery.application.port.in.ManageReviewUseCase.*;
import jjh.delivery.domain.review.Review;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

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
            @PathVariable UUID reviewId
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
            @PathVariable UUID reviewId,
            @Valid @RequestBody UpdateReviewRequest request
    ) {
        String customerId = userDetails.getUsername();

        UpdateReviewCommand command = new UpdateReviewCommand(
                reviewId.toString(),
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
            @PathVariable UUID reviewId
    ) {
        UUID customerId = UUID.fromString(userDetails.getUsername());
        manageReviewUseCase.deleteReview(reviewId, customerId);

        return ResponseEntity.noContent().build();
    }

    /**
     * 내 리뷰 목록 조회 (커서 기반 페이지네이션)
     *
     * @param cursor 이전 페이지의 nextCursor 값 (첫 페이지는 생략)
     */
    @GetMapping("/my")
    public ResponseEntity<ReviewListResponse> getMyReviews(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") int size
    ) {
        UUID customerId = UUID.fromString(userDetails.getUsername());
        CursorPageResponse<Review> reviews = manageReviewUseCase.getMyReviews(customerId, cursor, size);

        return ResponseEntity.ok(ReviewListResponse.from(reviews));
    }

    /**
     * 상품별 리뷰 목록 조회 (커서 기반 페이지네이션)
     *
     * @param cursor 이전 페이지의 nextCursor 값 (첫 페이지는 생략)
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<ReviewListResponse> getReviewsByProductId(
            @PathVariable UUID productId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") int size
    ) {
        CursorPageResponse<Review> reviews = manageReviewUseCase.getReviewsByProductId(productId, cursor, size);

        return ResponseEntity.ok(ReviewListResponse.from(reviews));
    }

    /**
     * 상품별 평점 정보 조회
     */
    @GetMapping("/product/{productId}/rating")
    public ResponseEntity<ReviewRatingInfoResponse> getProductRatingInfo(
            @PathVariable UUID productId
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
            @PathVariable UUID reviewId,
            @Valid @RequestBody AddReplyRequest request
    ) {
        UUID sellerId = UUID.fromString(userDetails.getUsername());
        Review review = manageReviewUseCase.addReply(reviewId, sellerId, request.content());

        return ResponseEntity.status(HttpStatus.CREATED).body(ReviewDetailResponse.from(review));
    }

    /**
     * 판매자 답글 수정
     */
    @PutMapping("/{reviewId}/reply")
    public ResponseEntity<ReviewDetailResponse> updateReply(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID reviewId,
            @Valid @RequestBody AddReplyRequest request
    ) {
        UUID sellerId = UUID.fromString(userDetails.getUsername());
        Review review = manageReviewUseCase.updateReply(reviewId, sellerId, request.content());

        return ResponseEntity.ok(ReviewDetailResponse.from(review));
    }

    /**
     * 판매자 답글 삭제
     */
    @DeleteMapping("/{reviewId}/reply")
    public ResponseEntity<ReviewDetailResponse> deleteReply(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID reviewId
    ) {
        UUID sellerId = UUID.fromString(userDetails.getUsername());
        Review review = manageReviewUseCase.deleteReply(reviewId, sellerId);

        return ResponseEntity.ok(ReviewDetailResponse.from(review));
    }
}
