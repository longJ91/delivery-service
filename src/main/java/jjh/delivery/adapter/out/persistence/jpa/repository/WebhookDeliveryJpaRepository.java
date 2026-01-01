package jjh.delivery.adapter.out.persistence.jpa.repository;

import jjh.delivery.adapter.out.persistence.jpa.entity.WebhookDeliveryJpaEntity;
import jjh.delivery.domain.webhook.WebhookDeliveryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Webhook Delivery JPA Repository
 */
@Repository
public interface WebhookDeliveryJpaRepository extends JpaRepository<WebhookDeliveryJpaEntity, String> {

    Page<WebhookDeliveryJpaEntity> findBySubscriptionId(String subscriptionId, Pageable pageable);

    @Query("SELECT d FROM WebhookDeliveryJpaEntity d WHERE d.status = :status AND d.nextRetryAt <= :now")
    List<WebhookDeliveryJpaEntity> findPendingRetries(
            @Param("status") WebhookDeliveryStatus status,
            @Param("now") LocalDateTime now
    );
}
