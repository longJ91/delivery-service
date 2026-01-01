package jjh.delivery.adapter.in.web.webhook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import jjh.delivery.domain.webhook.WebhookEventType;
import org.hibernate.validator.constraints.URL;

import java.util.Set;

/**
 * 웹훅 구독 생성 요청
 */
public record CreateWebhookRequest(
        @NotBlank(message = "웹훅 이름은 필수입니다")
        @Size(max = 100, message = "웹훅 이름은 100자 이내여야 합니다")
        String name,

        @NotBlank(message = "엔드포인트 URL은 필수입니다")
        @URL(message = "올바른 URL 형식이 아닙니다")
        @Size(max = 500, message = "URL은 500자 이내여야 합니다")
        String endpointUrl,

        @NotEmpty(message = "최소 하나의 이벤트 타입을 선택해야 합니다")
        Set<WebhookEventType> subscribedEvents
) {
}
