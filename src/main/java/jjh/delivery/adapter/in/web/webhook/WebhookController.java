package jjh.delivery.adapter.in.web.webhook;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import jjh.delivery.adapter.in.web.dto.CursorPageResponse;
import jjh.delivery.adapter.in.web.webhook.dto.*;
import jjh.delivery.application.port.in.ManageWebhookUseCase;
import jjh.delivery.application.port.in.ManageWebhookUseCase.*;
import jjh.delivery.domain.webhook.WebhookDelivery;
import jjh.delivery.domain.webhook.WebhookEventType;
import jjh.delivery.domain.webhook.WebhookSubscription;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Webhook REST Controller - Driving Adapter (Inbound)
 * 웹훅 관리 API
 */
@RestController
@RequestMapping("/api/v2/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final ManageWebhookUseCase manageWebhookUseCase;

    // ==================== 웹훅 구독 관리 ====================

    /**
     * 웹훅 구독 생성
     */
    @PostMapping
    public ResponseEntity<WebhookSubscriptionResponse> createSubscription(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateWebhookRequest request
    ) {
        String sellerId = userDetails.getUsername();

        CreateSubscriptionCommand command = new CreateSubscriptionCommand(
                sellerId,
                request.name(),
                request.endpointUrl(),
                request.subscribedEvents()
        );

        WebhookSubscription subscription = manageWebhookUseCase.createSubscription(command);

        return ResponseEntity.status(HttpStatus.CREATED).body(WebhookSubscriptionResponse.from(subscription));
    }

    /**
     * 웹훅 구독 수정
     */
    @PutMapping("/{subscriptionId}")
    public ResponseEntity<WebhookSubscriptionResponse> updateSubscription(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID subscriptionId,
            @Valid @RequestBody UpdateWebhookRequest request
    ) {
        String sellerId = userDetails.getUsername();

        UpdateSubscriptionCommand command = new UpdateSubscriptionCommand(
                sellerId,
                subscriptionId.toString(),
                request.name(),
                request.endpointUrl(),
                request.subscribedEvents()
        );

        WebhookSubscription subscription = manageWebhookUseCase.updateSubscription(command);

        return ResponseEntity.ok(WebhookSubscriptionResponse.fromWithMaskedSecret(subscription));
    }

    /**
     * 웹훅 구독 삭제
     */
    @DeleteMapping("/{subscriptionId}")
    public ResponseEntity<Void> deleteSubscription(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID subscriptionId
    ) {
        UUID sellerId = UUID.fromString(userDetails.getUsername());
        manageWebhookUseCase.deleteSubscription(sellerId, subscriptionId);

        return ResponseEntity.noContent().build();
    }

    /**
     * 웹훅 구독 활성화
     */
    @PostMapping("/{subscriptionId}/activate")
    public ResponseEntity<WebhookSubscriptionResponse> activateSubscription(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID subscriptionId
    ) {
        UUID sellerId = UUID.fromString(userDetails.getUsername());
        WebhookSubscription subscription = manageWebhookUseCase.activateSubscription(sellerId, subscriptionId);

        return ResponseEntity.ok(WebhookSubscriptionResponse.fromWithMaskedSecret(subscription));
    }

    /**
     * 웹훅 구독 비활성화
     */
    @PostMapping("/{subscriptionId}/deactivate")
    public ResponseEntity<WebhookSubscriptionResponse> deactivateSubscription(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID subscriptionId
    ) {
        UUID sellerId = UUID.fromString(userDetails.getUsername());
        WebhookSubscription subscription = manageWebhookUseCase.deactivateSubscription(sellerId, subscriptionId);

        return ResponseEntity.ok(WebhookSubscriptionResponse.fromWithMaskedSecret(subscription));
    }

    /**
     * 시크릿 재생성
     */
    @PostMapping("/{subscriptionId}/regenerate-secret")
    public ResponseEntity<WebhookSubscriptionResponse> regenerateSecret(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID subscriptionId
    ) {
        UUID sellerId = UUID.fromString(userDetails.getUsername());
        WebhookSubscription subscription = manageWebhookUseCase.regenerateSecret(sellerId, subscriptionId);

        // 새 시크릿은 마스킹 없이 반환
        return ResponseEntity.ok(WebhookSubscriptionResponse.from(subscription));
    }

    // ==================== 조회 ====================

    /**
     * 웹훅 구독 조회
     */
    @GetMapping("/{subscriptionId}")
    public ResponseEntity<WebhookSubscriptionResponse> getSubscription(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID subscriptionId
    ) {
        WebhookSubscription subscription = manageWebhookUseCase.getSubscription(subscriptionId);

        // 소유자 확인
        String sellerId = userDetails.getUsername();
        if (!subscription.getSellerId().equals(sellerId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(WebhookSubscriptionResponse.fromWithMaskedSecret(subscription));
    }

    /**
     * 내 웹훅 구독 목록 조회
     */
    @GetMapping("/my")
    public ResponseEntity<List<WebhookSubscriptionResponse>> getMySubscriptions(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID sellerId = UUID.fromString(userDetails.getUsername());
        List<WebhookSubscription> subscriptions = manageWebhookUseCase.getSellerSubscriptions(sellerId);

        List<WebhookSubscriptionResponse> response = subscriptions.stream()
                .map(WebhookSubscriptionResponse::fromWithMaskedSecret)
                .toList();

        return ResponseEntity.ok(response);
    }

    /**
     * 전송 기록 조회 (커서 기반 페이지네이션)
     *
     * @param cursor 이전 페이지의 nextCursor 값 (첫 페이지는 생략)
     */
    @GetMapping("/{subscriptionId}/deliveries")
    public ResponseEntity<WebhookDeliveryListResponse> getDeliveryHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID subscriptionId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size
    ) {
        // 소유자 확인
        WebhookSubscription subscription = manageWebhookUseCase.getSubscription(subscriptionId);
        String sellerId = userDetails.getUsername();
        if (!subscription.getSellerId().equals(UUID.fromString(sellerId))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        CursorPageResponse<WebhookDelivery> deliveries = manageWebhookUseCase.getDeliveryHistory(subscriptionId, cursor, size);

        return ResponseEntity.ok(WebhookDeliveryListResponse.from(deliveries));
    }

    /**
     * 웹훅 테스트
     */
    @PostMapping("/{subscriptionId}/test")
    public ResponseEntity<WebhookDeliveryResponse> testWebhook(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID subscriptionId
    ) {
        UUID sellerId = UUID.fromString(userDetails.getUsername());
        WebhookDelivery delivery = manageWebhookUseCase.testWebhook(sellerId, subscriptionId);

        return ResponseEntity.ok(WebhookDeliveryResponse.from(delivery));
    }

    // ==================== 이벤트 타입 조회 ====================

    /**
     * 지원하는 이벤트 타입 목록 조회
     */
    @GetMapping("/event-types")
    public ResponseEntity<List<EventTypeResponse>> getEventTypes() {
        List<EventTypeResponse> eventTypes = Arrays.stream(WebhookEventType.values())
                .map(type -> new EventTypeResponse(type.name(), type.getDescription()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(eventTypes);
    }

    public record EventTypeResponse(String code, String description) {
    }
}
