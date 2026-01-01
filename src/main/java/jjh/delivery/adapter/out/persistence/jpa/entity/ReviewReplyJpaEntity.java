package jjh.delivery.adapter.out.persistence.jpa.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Review Reply JPA Entity
 * Seller's response to a customer review
 */
@Entity
@Table(name = "review_replies", indexes = {
        @Index(name = "idx_review_replies_seller", columnList = "seller_id")
})
public class ReviewReplyJpaEntity {

    @Id
    @Column(length = 36)
    private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false, unique = true)
    private ReviewJpaEntity review;

    @Column(name = "seller_id", nullable = false, length = 36)
    private String sellerId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected ReviewReplyJpaEntity() {
    }

    public ReviewReplyJpaEntity(
            String id,
            String sellerId,
            String content,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.sellerId = sellerId;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public String getId() {
        return id;
    }

    public ReviewJpaEntity getReview() {
        return review;
    }

    public String getSellerId() {
        return sellerId;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // Setters
    public void setReview(ReviewJpaEntity review) {
        this.review = review;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
