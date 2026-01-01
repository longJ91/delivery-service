package jjh.delivery.adapter.in.web.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.List;

/**
 * 리뷰 수정 요청
 */
public record UpdateReviewRequest(

        @Min(value = 1, message = "평점은 1 이상이어야 합니다")
        @Max(value = 5, message = "평점은 5 이하여야 합니다")
        int rating,

        String content,

        List<String> imageUrls

) {}
