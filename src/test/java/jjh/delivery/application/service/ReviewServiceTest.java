package jjh.delivery.application.service;

import jjh.delivery.application.port.in.ManageReviewUseCase.CreateReviewCommand;
import jjh.delivery.application.port.in.ManageReviewUseCase.ReviewRatingInfo;
import jjh.delivery.application.port.in.ManageReviewUseCase.UpdateReviewCommand;
import jjh.delivery.application.port.out.LoadReviewPort;
import jjh.delivery.application.port.out.LoadReviewStatsPort;
import jjh.delivery.application.port.out.SaveReviewPort;
import jjh.delivery.domain.review.Review;
import jjh.delivery.domain.review.ReviewReply;
import jjh.delivery.domain.review.exception.ReviewNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * ReviewService Unit Tests
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewService 테스트")
class ReviewServiceTest {

    @Mock
    private LoadReviewPort loadReviewPort;

    @Mock
    private LoadReviewStatsPort loadReviewStatsPort;

    @Mock
    private SaveReviewPort saveReviewPort;

    @InjectMocks
    private ReviewService reviewService;

    // =====================================================
    // Test Fixtures
    // =====================================================

    private static final UUID REVIEW_ID = UUID.fromString("00000000-0000-0000-0000-000000000010");
    private static final UUID ORDER_ID = UUID.fromString("00000000-0000-0000-0000-000000000011");
    private static final UUID CUSTOMER_ID = UUID.fromString("00000000-0000-0000-0000-000000000012");
    private static final UUID SELLER_ID = UUID.fromString("00000000-0000-0000-0000-000000000013");
    private static final UUID PRODUCT_ID = UUID.fromString("00000000-0000-0000-0000-000000000014");
    private static final UUID NON_EXISTENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000099");
    private static final UUID OTHER_CUSTOMER_ID = UUID.fromString("00000000-0000-0000-0000-000000000098");

    private Review createReview() {
        return Review.builder()
                .id(REVIEW_ID)
                .orderId(ORDER_ID)
                .customerId(CUSTOMER_ID)
                .sellerId(SELLER_ID)
                .productId(PRODUCT_ID)
                .rating(5)
                .content("좋은 상품입니다!")
                .build();
    }

    private Review createReviewWithReply() {
        Review review = createReview();
        review.addReply(SELLER_ID, "감사합니다!");
        return review;
    }

    private CreateReviewCommand createReviewCommand() {
        return new CreateReviewCommand(
                CUSTOMER_ID.toString(),
                ORDER_ID.toString(),
                SELLER_ID.toString(),
                PRODUCT_ID.toString(),
                5,
                "좋은 상품입니다!",
                List.of("http://image1.jpg", "http://image2.jpg")
        );
    }

    // =====================================================
    // 리뷰 생성 테스트
    // =====================================================

    @Nested
    @DisplayName("리뷰 생성")
    class CreateReview {

        @Test
        @DisplayName("리뷰 생성 성공")
        void createReviewSuccess() {
            // given
            CreateReviewCommand command = createReviewCommand();

            given(loadReviewPort.existsByOrderId(ORDER_ID))
                    .willReturn(false);
            given(saveReviewPort.save(any(Review.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            Review result = reviewService.createReview(command);

            // then
            assertThat(result.getCustomerId()).isEqualTo(CUSTOMER_ID);
            assertThat(result.getProductId()).isEqualTo(PRODUCT_ID);
            assertThat(result.getRating()).isEqualTo(5);
            assertThat(result.getContent()).isEqualTo("좋은 상품입니다!");
            assertThat(result.getImages()).hasSize(2);
            verify(saveReviewPort).save(any(Review.class));
        }

        @Test
        @DisplayName("이미 리뷰가 존재하면 예외")
        void createReviewDuplicateThrowsException() {
            // given
            CreateReviewCommand command = createReviewCommand();

            given(loadReviewPort.existsByOrderId(ORDER_ID))
                    .willReturn(true);

            // when & then
            assertThatThrownBy(() -> reviewService.createReview(command))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("이미 해당 주문에 대한 리뷰가 존재");
        }

        @Test
        @DisplayName("이미지 없이 리뷰 생성 성공")
        void createReviewWithoutImagesSuccess() {
            // given
            CreateReviewCommand command = new CreateReviewCommand(
                    CUSTOMER_ID.toString(), ORDER_ID.toString(), SELLER_ID.toString(), PRODUCT_ID.toString(), 4, "괜찮아요", null
            );

            given(loadReviewPort.existsByOrderId(ORDER_ID))
                    .willReturn(false);
            given(saveReviewPort.save(any(Review.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            Review result = reviewService.createReview(command);

            // then
            assertThat(result.getImages()).isEmpty();
        }
    }

    // =====================================================
    // 리뷰 수정 테스트
    // =====================================================

    @Nested
    @DisplayName("리뷰 수정")
    class UpdateReview {

        @Test
        @DisplayName("리뷰 수정 성공")
        void updateReviewSuccess() {
            // given
            Review review = createReview();
            UpdateReviewCommand command = new UpdateReviewCommand(
                    REVIEW_ID.toString(), CUSTOMER_ID.toString(), 4, "수정된 내용", null
            );

            given(loadReviewPort.findById(REVIEW_ID))
                    .willReturn(Optional.of(review));
            given(saveReviewPort.save(any(Review.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            Review result = reviewService.updateReview(command);

            // then
            assertThat(result.getRating()).isEqualTo(4);
            assertThat(result.getContent()).isEqualTo("수정된 내용");
        }

        @Test
        @DisplayName("다른 사람의 리뷰 수정 시 예외")
        void updateReviewNotOwnerThrowsException() {
            // given
            Review review = createReview();
            UpdateReviewCommand command = new UpdateReviewCommand(
                    REVIEW_ID.toString(), OTHER_CUSTOMER_ID.toString(), 4, "수정 시도", null
            );

            given(loadReviewPort.findById(REVIEW_ID))
                    .willReturn(Optional.of(review));

            // when & then
            assertThatThrownBy(() -> reviewService.updateReview(command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("리뷰를 수정할 권한이 없습니다");
        }

        @Test
        @DisplayName("존재하지 않는 리뷰 수정 시 예외")
        void updateReviewNotFoundThrowsException() {
            // given
            UpdateReviewCommand command = new UpdateReviewCommand(
                    NON_EXISTENT_ID.toString(), CUSTOMER_ID.toString(), 4, "내용", null
            );

            given(loadReviewPort.findById(NON_EXISTENT_ID))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> reviewService.updateReview(command))
                    .isInstanceOf(ReviewNotFoundException.class);
        }
    }

    // =====================================================
    // 리뷰 삭제 테스트
    // =====================================================

    @Nested
    @DisplayName("리뷰 삭제")
    class DeleteReview {

        @Test
        @DisplayName("리뷰 삭제 성공")
        void deleteReviewSuccess() {
            // given
            Review review = createReview();

            given(loadReviewPort.findById(REVIEW_ID))
                    .willReturn(Optional.of(review));

            // when
            reviewService.deleteReview(REVIEW_ID, CUSTOMER_ID);

            // then
            verify(saveReviewPort).delete(REVIEW_ID);
        }

        @Test
        @DisplayName("다른 사람의 리뷰 삭제 시 예외")
        void deleteReviewNotOwnerThrowsException() {
            // given
            Review review = createReview();

            given(loadReviewPort.findById(REVIEW_ID))
                    .willReturn(Optional.of(review));

            // when & then
            assertThatThrownBy(() -> reviewService.deleteReview(REVIEW_ID, OTHER_CUSTOMER_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("리뷰를 삭제할 권한이 없습니다");
        }
    }

    // =====================================================
    // 리뷰 조회 테스트
    // =====================================================

    @Nested
    @DisplayName("리뷰 조회")
    class GetReview {

        @Test
        @DisplayName("리뷰 조회 성공")
        void getReviewSuccess() {
            // given
            Review review = createReview();

            given(loadReviewPort.findById(REVIEW_ID))
                    .willReturn(Optional.of(review));

            // when
            Review result = reviewService.getReview(REVIEW_ID);

            // then
            assertThat(result.getId()).isEqualTo(REVIEW_ID);
        }

        @Test
        @DisplayName("상품별 리뷰 목록 조회")
        void getReviewsByProductIdSuccess() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Review> reviews = new PageImpl<>(List.of(createReview()));

            given(loadReviewPort.findByProductId(PRODUCT_ID, pageable))
                    .willReturn(reviews);

            // when
            Page<Review> result = reviewService.getReviewsByProductId(PRODUCT_ID, pageable);

            // then
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("내 리뷰 목록 조회")
        void getMyReviewsSuccess() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Review> reviews = new PageImpl<>(List.of(createReview()));

            given(loadReviewPort.findByCustomerId(CUSTOMER_ID, pageable))
                    .willReturn(reviews);

            // when
            Page<Review> result = reviewService.getMyReviews(CUSTOMER_ID, pageable);

            // then
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("상품 평점 정보 조회")
        void getProductRatingInfoSuccess() {
            // given
            given(loadReviewStatsPort.getAverageRatingByProductId(PRODUCT_ID))
                    .willReturn(4.5);
            given(loadReviewPort.countByProductId(PRODUCT_ID))
                    .willReturn(100L);
            given(loadReviewStatsPort.getRatingDistributionByProductId(PRODUCT_ID))
                    .willReturn(Map.of(5, 60L, 4, 30L, 3, 10L));

            // when
            ReviewRatingInfo result = reviewService.getProductRatingInfo(PRODUCT_ID);

            // then
            assertThat(result.averageRating()).isEqualTo(4.5);
            assertThat(result.totalCount()).isEqualTo(100L);
            assertThat(result.ratingDistribution()).containsKey(5);
        }
    }

    // =====================================================
    // 판매자 답글 테스트
    // =====================================================

    @Nested
    @DisplayName("판매자 답글")
    class SellerReply {

        @Test
        @DisplayName("답글 추가 성공")
        void addReplySuccess() {
            // given
            Review review = createReview();

            given(loadReviewPort.findById(REVIEW_ID))
                    .willReturn(Optional.of(review));
            given(saveReviewPort.save(any(Review.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            Review result = reviewService.addReply(REVIEW_ID, SELLER_ID, "감사합니다!");

            // then
            assertThat(result.hasReply()).isTrue();
            assertThat(result.getReply().content()).isEqualTo("감사합니다!");
        }

        @Test
        @DisplayName("답글 수정 성공")
        void updateReplySuccess() {
            // given
            Review review = createReviewWithReply();

            given(loadReviewPort.findById(REVIEW_ID))
                    .willReturn(Optional.of(review));
            given(saveReviewPort.save(any(Review.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            Review result = reviewService.updateReply(REVIEW_ID, SELLER_ID, "수정된 답글");

            // then
            assertThat(result.getReply().content()).isEqualTo("수정된 답글");
        }

        @Test
        @DisplayName("답글 삭제 성공")
        void deleteReplySuccess() {
            // given
            Review review = createReviewWithReply();

            given(loadReviewPort.findById(REVIEW_ID))
                    .willReturn(Optional.of(review));
            given(saveReviewPort.save(any(Review.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            Review result = reviewService.deleteReply(REVIEW_ID, SELLER_ID);

            // then
            assertThat(result.hasReply()).isFalse();
        }

        @Test
        @DisplayName("존재하지 않는 리뷰에 답글 추가 시 예외")
        void addReplyNotFoundThrowsException() {
            // given
            given(loadReviewPort.findById(NON_EXISTENT_ID))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> reviewService.addReply(NON_EXISTENT_ID, SELLER_ID, "답글"))
                    .isInstanceOf(ReviewNotFoundException.class);
        }
    }
}
