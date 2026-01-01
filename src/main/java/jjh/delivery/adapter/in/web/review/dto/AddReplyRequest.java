package jjh.delivery.adapter.in.web.review.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 판매자 답글 추가 요청
 */
public record AddReplyRequest(

        @NotBlank(message = "답글 내용은 필수입니다")
        String content

) {}
