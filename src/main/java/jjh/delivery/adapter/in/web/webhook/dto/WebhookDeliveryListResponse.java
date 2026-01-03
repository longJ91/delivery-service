package jjh.delivery.adapter.in.web.webhook.dto;

import jjh.delivery.adapter.in.web.dto.CursorPageResponse;
import jjh.delivery.domain.webhook.WebhookDelivery;

import java.util.List;

/**
 * 웹훅 전송 기록 목록 응답 (커서 기반 페이지네이션)
 */
public record WebhookDeliveryListResponse(
        List<WebhookDeliveryResponse> content,
        int size,
        boolean hasNext,
        String nextCursor
) {
    public static WebhookDeliveryListResponse from(CursorPageResponse<WebhookDelivery> cursorPage) {
        List<WebhookDeliveryResponse> content = cursorPage.content().stream()
                .map(WebhookDeliveryResponse::from)
                .toList();

        return new WebhookDeliveryListResponse(
                content,
                cursorPage.size(),
                cursorPage.hasNext(),
                cursorPage.nextCursor()
        );
    }
}
