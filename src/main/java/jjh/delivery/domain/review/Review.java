package jjh.delivery.domain.review;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Review Aggregate Root
 * 순수 도메인 객체 - 외부 프레임워크 의존성 없음
 */
public class Review {

    private final UUID id;
    private final UUID orderId;
    private final UUID customerId;
    private final UUID sellerId;
    private final UUID productId;
    private int rating;
    private String content;
    private List<ReviewImage> images;
    private ReviewReply reply;
    private boolean isVisible;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Review(Builder builder) {
        this.id = builder.id != null ? builder.id : UUID.randomUUID();
        this.orderId = builder.orderId;
        this.customerId = builder.customerId;
        this.sellerId = builder.sellerId;
        this.productId = builder.productId;
        this.rating = builder.rating;
        this.content = builder.content;
        this.images = builder.images != null ? new ArrayList<>(builder.images) : new ArrayList<>();
        this.reply = builder.reply;
        this.isVisible = builder.isVisible;
        this.createdAt = builder.createdAt != null ? builder.createdAt : LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    // =====================================================
    // Domain Business Logic
    // =====================================================

    /**
     * 리뷰 내용 수정
     */
    public void updateContent(int rating, String content) {
        validateRating(rating);
        this.rating = rating;
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 이미지 추가
     */
    public void addImage(String imageUrl) {
        int nextOrder = this.images.size();
        this.images.add(ReviewImage.ofNew(UUID.randomUUID(), imageUrl, nextOrder));
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 이미지 전체 교체
     */
    public void replaceImages(List<ReviewImage> newImages) {
        this.images = new ArrayList<>(newImages);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 이미지 삭제
     */
    public void removeImage(UUID imageId) {
        this.images.removeIf(image -> image.id().equals(imageId));
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 판매자 답글 추가
     */
    public void addReply(UUID sellerId, String content) {
        if (!this.sellerId.equals(sellerId)) {
            throw new IllegalArgumentException("Only the seller of this review can reply");
        }
        if (this.reply != null) {
            throw new IllegalStateException("Reply already exists. Use updateReply instead.");
        }
        this.reply = ReviewReply.create(sellerId, content);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 판매자 답글 수정
     */
    public void updateReply(UUID sellerId, String content) {
        if (this.reply == null) {
            throw new IllegalStateException("No reply exists. Use addReply instead.");
        }
        if (!this.sellerId.equals(sellerId)) {
            throw new IllegalArgumentException("Only the seller of this review can update reply");
        }
        this.reply = this.reply.update(content);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 판매자 답글 삭제
     */
    public void deleteReply(UUID sellerId) {
        if (this.reply == null) {
            throw new IllegalStateException("No reply exists to delete");
        }
        if (!this.sellerId.equals(sellerId)) {
            throw new IllegalArgumentException("Only the seller of this review can delete reply");
        }
        this.reply = null;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 리뷰 숨기기 (관리자/판매자용)
     */
    public void hide() {
        this.isVisible = false;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 리뷰 보이기
     */
    public void show() {
        this.isVisible = true;
        this.updatedAt = LocalDateTime.now();
    }

    // =====================================================
    // Query Methods
    // =====================================================

    /**
     * 답글 존재 여부
     */
    public boolean hasReply() {
        return reply != null;
    }

    /**
     * 이미지 존재 여부
     */
    public boolean hasImages() {
        return !images.isEmpty();
    }

    /**
     * 이미지 개수
     */
    public int getImageCount() {
        return images.size();
    }

    // =====================================================
    // Validation
    // =====================================================

    private void validateRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
    }

    // =====================================================
    // Getters
    // =====================================================

    public UUID getId() {
        return id;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public UUID getSellerId() {
        return sellerId;
    }

    public UUID getProductId() {
        return productId;
    }

    public int getRating() {
        return rating;
    }

    public String getContent() {
        return content;
    }

    public List<ReviewImage> getImages() {
        return Collections.unmodifiableList(images);
    }

    public ReviewReply getReply() {
        return reply;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // =====================================================
    // Builder
    // =====================================================

    public static class Builder {
        private UUID id;
        private UUID orderId;
        private UUID customerId;
        private UUID sellerId;
        private UUID productId;
        private int rating;
        private String content;
        private List<ReviewImage> images;
        private ReviewReply reply;
        private boolean isVisible = true;
        private LocalDateTime createdAt;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder orderId(UUID orderId) {
            this.orderId = orderId;
            return this;
        }

        public Builder customerId(UUID customerId) {
            this.customerId = customerId;
            return this;
        }

        public Builder sellerId(UUID sellerId) {
            this.sellerId = sellerId;
            return this;
        }

        public Builder productId(UUID productId) {
            this.productId = productId;
            return this;
        }

        public Builder rating(int rating) {
            this.rating = rating;
            return this;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder images(List<ReviewImage> images) {
            this.images = images;
            return this;
        }

        public Builder reply(ReviewReply reply) {
            this.reply = reply;
            return this;
        }

        public Builder isVisible(boolean isVisible) {
            this.isVisible = isVisible;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Review build() {
            validateRequired();
            return new Review(this);
        }

        private void validateRequired() {
            if (orderId == null) {
                throw new IllegalArgumentException("orderId is required");
            }
            if (customerId == null) {
                throw new IllegalArgumentException("customerId is required");
            }
            if (sellerId == null) {
                throw new IllegalArgumentException("sellerId is required");
            }
            if (productId == null) {
                throw new IllegalArgumentException("productId is required");
            }
            if (rating < 1 || rating > 5) {
                throw new IllegalArgumentException("rating must be between 1 and 5");
            }
        }
    }
}
