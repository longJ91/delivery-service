package jjh.delivery.adapter.out.persistence.jpa.repository;

import jjh.delivery.adapter.out.persistence.jpa.entity.WebhookSubscriptionJpaEntity;
import jjh.delivery.domain.webhook.WebhookEventType;
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
 * Webhook Subscription JPA Repository
 */
@Repository
public interface WebhookSubscriptionJpaRepository extends JpaRepository<WebhookSubscriptionJpaEntity, UUID> {

    List<WebhookSubscriptionJpaEntity> findBySellerId(UUID sellerId);

    @Query("SELECT w FROM WebhookSubscriptionJpaEntity w WHERE w.isActive = true AND :eventType MEMBER OF w.subscribedEvents")
    List<WebhookSubscriptionJpaEntity> findActiveByEventType(@Param("eventType") WebhookEventType eventType);

    // ==================== Cursor-based Pagination ====================

    /**
     * 전체 구독 조회 (커서 기반)
     */
    @Query("SELECT w FROM WebhookSubscriptionJpaEntity w " +
            "WHERE (w.createdAt < :cursorCreatedAt OR (w.createdAt = :cursorCreatedAt AND w.id < :cursorId)) " +
            "ORDER BY w.createdAt DESC, w.id DESC")
    List<WebhookSubscriptionJpaEntity> findAllWithCursor(
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") UUID cursorId,
            Pageable pageable);

    default List<WebhookSubscriptionJpaEntity> findAllWithCursor(LocalDateTime cursorCreatedAt, UUID cursorId, int limit) {
        return findAllWithCursor(cursorCreatedAt, cursorId, PageRequest.of(0, limit));
    }

    /**
     * 전체 구독 조회 (첫 페이지)
     */
    @Query("SELECT w FROM WebhookSubscriptionJpaEntity w ORDER BY w.createdAt DESC, w.id DESC")
    List<WebhookSubscriptionJpaEntity> findAllOrderByCreatedAtDesc(Pageable pageable);

    default List<WebhookSubscriptionJpaEntity> findAllOrderByCreatedAtDesc(int limit) {
        return findAllOrderByCreatedAtDesc(PageRequest.of(0, limit));
    }
}
