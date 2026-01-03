package jjh.delivery.adapter.out.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Review JPA Entity
 */
@Entity
@Table(name = "reviews", indexes = {
        @Index(name = "idx_reviews_customer", columnList = "customer_id"),
        @Index(name = "idx_reviews_seller", columnList = "seller_id"),
        @Index(name = "idx_reviews_product", columnList = "product_id"),
        @Index(name = "idx_reviews_rating", columnList = "rating")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewJpaEntity {

    @Id
    private UUID id;

    @Column(name = "order_id", nullable = false, unique = true)
    private UUID orderId;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "seller_id", nullable = false)
    private UUID sellerId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(nullable = false)
    private int rating;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_visible", nullable = false)
    private boolean isVisible;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<ReviewImageJpaEntity> images = new ArrayList<>();

    @OneToOne(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private ReviewReplyJpaEntity reply;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public ReviewJpaEntity(
            UUID id,
            UUID orderId,
            UUID customerId,
            UUID sellerId,
            UUID productId,
            int rating,
            String content,
            boolean isVisible,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.orderId = orderId;
        this.customerId = customerId;
        this.sellerId = sellerId;
        this.productId = productId;
        this.rating = rating;
        this.content = content;
        this.isVisible = isVisible;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Setters for update
    public void setRating(int rating) {
        this.rating = rating;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    // Image management
    public void addImage(ReviewImageJpaEntity image) {
        images.add(image);
        image.setReview(this);
    }

    public void removeImage(ReviewImageJpaEntity image) {
        images.remove(image);
        image.setReview(null);
    }

    public void clearImages() {
        images.forEach(image -> image.setReview(null));
        images.clear();
    }

    // Reply management
    public void setReply(ReviewReplyJpaEntity reply) {
        if (reply == null) {
            if (this.reply != null) {
                this.reply.setReview(null);
            }
        } else {
            reply.setReview(this);
        }
        this.reply = reply;
    }
}
