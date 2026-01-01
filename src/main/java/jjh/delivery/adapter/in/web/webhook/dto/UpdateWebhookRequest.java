package jjh.delivery.adapter.in.web.webhook.dto;

import jakarta.validation.constraints.Size;
import jjh.delivery.domain.webhook.WebhookEventType;
import org.hibernate.validator.constraints.URL;

import java.util.Set;

/**
 * 웹훅 구독 수정 요청
 */
public record UpdateWebhookRequest(
        @Size(max = 100, message = "웹훅 이름은 100자 이내여야 합니다")
        String name,

        @URL(message = "올바른 URL 형식이 아닙니다")
        @Size(max = 500, message = "URL은 500자 이내여야 합니다")
        String endpointUrl,

        Set<WebhookEventType> subscribedEvents
) {
}
