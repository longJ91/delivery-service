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

    private final String id;
    private final String orderId;
    private final String customerId;
    private final String sellerId;
    private final String productId;
    private int rating;
    private String content;
    private List<ReviewImage> images;
    private ReviewReply reply;
    private boolean isVisible;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Review(Builder builder) {
        this.id = builder.id != null ? builder.id : UUID.randomUUID().toString();
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
        this.images.add(ReviewImage.ofNew(imageUrl, nextOrder));
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
    public void removeImage(String imageId) {
        this.images.removeIf(image -> image.id().equals(imageId));
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 판매자 답글 추가
     */
    public void addReply(String sellerId, String content) {
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
    public void updateReply(String sellerId, String content) {
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
    public void deleteReply(String sellerId) {
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
        private String id;
        private String orderId;
        private String customerId;
        private String sellerId;
        private String productId;
        private int rating;
        private String content;
        private List<ReviewImage> images;
        private ReviewReply reply;
        private boolean isVisible = true;
        private LocalDateTime createdAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder orderId(String orderId) {
            this.orderId = orderId;
            return this;
        }

        public Builder customerId(String customerId) {
            this.customerId = customerId;
            return this;
        }

        public Builder sellerId(String sellerId) {
            this.sellerId = sellerId;
            return this;
        }

        public Builder productId(String productId) {
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
            if (orderId == null || orderId.isBlank()) {
                throw new IllegalArgumentException("orderId is required");
            }
            if (customerId == null || customerId.isBlank()) {
                throw new IllegalArgumentException("customerId is required");
            }
            if (sellerId == null || sellerId.isBlank()) {
                throw new IllegalArgumentException("sellerId is required");
            }
            if (productId == null || productId.isBlank()) {
                throw new IllegalArgumentException("productId is required");
            }
            if (rating < 1 || rating > 5) {
                throw new IllegalArgumentException("rating must be between 1 and 5");
            }
        }
    }
}
