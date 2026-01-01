package jjh.delivery.adapter.out.persistence.jpa.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
public class ReviewJpaEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "order_id", nullable = false, unique = true, length = 36)
    private String orderId;

    @Column(name = "customer_id", nullable = false, length = 36)
    private String customerId;

    @Column(name = "seller_id", nullable = false, length = 36)
    private String sellerId;

    @Column(name = "product_id", nullable = false, length = 36)
    private String productId;

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

    protected ReviewJpaEntity() {
    }

    public ReviewJpaEntity(
            String id,
            String orderId,
            String customerId,
            String sellerId,
            String productId,
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

    // Getters
    public String getId() {
        return id;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getSellerId() {
        return sellerId;
    }

    public String getProductId() {
        return productId;
    }

    public int getRating() {
        return rating;
    }

    public String getContent() {
        return content;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public List<ReviewImageJpaEntity> getImages() {
        return images;
    }

    public ReviewReplyJpaEntity getReply() {
        return reply;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
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
