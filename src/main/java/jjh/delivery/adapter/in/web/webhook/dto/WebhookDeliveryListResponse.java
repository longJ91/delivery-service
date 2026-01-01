package jjh.delivery.adapter.in.web.webhook.dto;

import jjh.delivery.domain.webhook.WebhookDelivery;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 웹훅 전송 기록 목록 응답
 */
public record WebhookDeliveryListResponse(
        List<WebhookDeliveryResponse> deliveries,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
    public static WebhookDeliveryListResponse from(Page<WebhookDelivery> page) {
        List<WebhookDeliveryResponse> deliveries = page.getContent().stream()
                .map(WebhookDeliveryResponse::from)
                .toList();

        return new WebhookDeliveryListResponse(
                deliveries,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext()
        );
    }
}
