package jjh.delivery.adapter.out.persistence.jpa.repository;

import jjh.delivery.adapter.out.persistence.jpa.entity.WebhookDeliveryJpaEntity;
import jjh.delivery.domain.webhook.WebhookDeliveryStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Webhook Delivery JPA Repository
 */
@Repository
public interface WebhookDeliveryJpaRepository extends JpaRepository<WebhookDeliveryJpaEntity, UUID> {

    @Query("SELECT d FROM WebhookDeliveryJpaEntity d WHERE d.status = :status AND d.nextRetryAt <= :now")
    List<WebhookDeliveryJpaEntity> findPendingRetries(
            @Param("status") WebhookDeliveryStatus status,
            @Param("now") LocalDateTime now
    );

    // ==================== Cursor-based Pagination ====================

    /**
     * 구독별 전송 기록 조회 (커서 기반)
     */
    @Query("SELECT d FROM WebhookDeliveryJpaEntity d WHERE d.subscriptionId = :subscriptionId " +
            "AND (d.createdAt < :cursorCreatedAt OR (d.createdAt = :cursorCreatedAt AND d.id < :cursorId)) " +
            "ORDER BY d.createdAt DESC, d.id DESC")
    List<WebhookDeliveryJpaEntity> findBySubscriptionIdWithCursor(
            @Param("subscriptionId") UUID subscriptionId,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") UUID cursorId,
            Pageable pageable);

    default List<WebhookDeliveryJpaEntity> findBySubscriptionIdWithCursor(
            UUID subscriptionId, LocalDateTime cursorCreatedAt, UUID cursorId, int limit) {
        return findBySubscriptionIdWithCursor(subscriptionId, cursorCreatedAt, cursorId, PageRequest.of(0, limit));
    }

    /**
     * 구독별 전송 기록 조회 (첫 페이지)
     */
    @Query("SELECT d FROM WebhookDeliveryJpaEntity d WHERE d.subscriptionId = :subscriptionId " +
            "ORDER BY d.createdAt DESC, d.id DESC")
    List<WebhookDeliveryJpaEntity> findBySubscriptionIdOrderByCreatedAtDesc(
            @Param("subscriptionId") UUID subscriptionId,
            Pageable pageable);

    default List<WebhookDeliveryJpaEntity> findBySubscriptionIdOrderByCreatedAtDesc(UUID subscriptionId, int limit) {
        return findBySubscriptionIdOrderByCreatedAtDesc(subscriptionId, PageRequest.of(0, limit));
    }
}
