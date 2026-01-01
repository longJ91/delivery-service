package jjh.delivery.adapter.in.web.review.dto;

import jakarta.validation.constraints.*;

import java.util.List;

/**
 * 리뷰 작성 요청
 */
public record CreateReviewRequest(

        @NotBlank(message = "주문 ID는 필수입니다")
        String orderId,

        @NotBlank(message = "판매자 ID는 필수입니다")
        String sellerId,

        @NotBlank(message = "상품 ID는 필수입니다")
        String productId,

        @Min(value = 1, message = "평점은 1 이상이어야 합니다")
        @Max(value = 5, message = "평점은 5 이하여야 합니다")
        int rating,

        String content,

        List<String> imageUrls

) {}
