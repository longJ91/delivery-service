package jjh.delivery.domain.outbox;

/**
 * Outbox Event Status
 * 이벤트 발행 상태 관리
 */
public enum OutboxStatus {

    /** 발행 대기 중 */
    PENDING,

    /** 발행 완료 */
    SENT,

    /** 발행 실패 (재시도 한도 초과) */
    FAILED
}
