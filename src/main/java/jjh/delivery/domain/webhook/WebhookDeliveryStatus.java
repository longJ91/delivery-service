package jjh.delivery.domain.webhook;

/**
 * Webhook Delivery Status Enum
 * 웹훅 전송 상태
 */
public enum WebhookDeliveryStatus {

    /** 전송 대기 */
    PENDING,

    /** 전송 완료 */
    DELIVERED,

    /** 재시도 대기 */
    RETRYING,

    /** 전송 실패 */
    FAILED;

    /**
     * 성공 상태인지 확인
     */
    public boolean isSuccess() {
        return this == DELIVERED;
    }

    /**
     * 최종 상태인지 확인
     */
    public boolean isFinal() {
        return this == DELIVERED || this == FAILED;
    }
}
