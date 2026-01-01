package jjh.delivery.adapter.out.persistence.jpa.repository;

import jjh.delivery.adapter.out.persistence.jpa.entity.WebhookSubscriptionJpaEntity;
import jjh.delivery.domain.webhook.WebhookEventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Webhook Subscription JPA Repository
 */
@Repository
public interface WebhookSubscriptionJpaRepository extends JpaRepository<WebhookSubscriptionJpaEntity, String> {

    List<WebhookSubscriptionJpaEntity> findBySellerId(String sellerId);

    @Query("SELECT w FROM WebhookSubscriptionJpaEntity w WHERE w.isActive = true AND :eventType MEMBER OF w.subscribedEvents")
    List<WebhookSubscriptionJpaEntity> findActiveByEventType(@Param("eventType") WebhookEventType eventType);
}
