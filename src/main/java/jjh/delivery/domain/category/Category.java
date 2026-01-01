package jjh.delivery.domain.category;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Category Aggregate Root
 * 순수 도메인 객체 - 외부 프레임워크 의존성 없음
 */
public class Category {

    private final String id;
    private final String parentId;
    private String name;
    private String description;
    private String imageUrl;
    private int displayOrder;
    private int depth;
    private boolean isActive;
    private final List<Category> children;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Category(Builder builder) {
        this.id = builder.id != null ? builder.id : UUID.randomUUID().toString();
        this.parentId = builder.parentId;
        this.name = builder.name;
        this.description = builder.description;
        this.imageUrl = builder.imageUrl;
        this.displayOrder = builder.displayOrder;
        this.depth = builder.depth;
        this.isActive = builder.isActive;
        this.children = new ArrayList<>(builder.children);
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
     * 카테고리 정보 수정
     */
    public void updateInfo(String name, String description, String imageUrl) {
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 표시 순서 변경
     */
    public void updateDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 카테고리 활성화
     */
    public void activate() {
        this.isActive = true;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 카테고리 비활성화
     */
    public void deactivate() {
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 자식 카테고리 추가
     */
    public void addChild(Category child) {
        this.children.add(child);
    }

    // =====================================================
    // Query Methods
    // =====================================================

    /**
     * 최상위 카테고리인지 확인
     */
    public boolean isRoot() {
        return parentId == null;
    }

    /**
     * 자식 카테고리가 있는지 확인
     */
    public boolean hasChildren() {
        return !children.isEmpty();
    }

    // =====================================================
    // Getters
    // =====================================================

    public String getId() {
        return id;
    }

    public String getParentId() {
        return parentId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public int getDepth() {
        return depth;
    }

    public boolean isActive() {
        return isActive;
    }

    public List<Category> getChildren() {
        return Collections.unmodifiableList(children);
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
        private String parentId;
        private String name;
        private String description;
        private String imageUrl;
        private int displayOrder;
        private int depth = 1;
        private boolean isActive = true;
        private List<Category> children = new ArrayList<>();
        private LocalDateTime createdAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder parentId(String parentId) {
            this.parentId = parentId;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder imageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
            return this;
        }

        public Builder displayOrder(int displayOrder) {
            this.displayOrder = displayOrder;
            return this;
        }

        public Builder depth(int depth) {
            this.depth = depth;
            return this;
        }

        public Builder isActive(boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public Builder children(List<Category> children) {
            this.children = new ArrayList<>(children);
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Category build() {
            validateRequired();
            return new Category(this);
        }

        private void validateRequired() {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("name is required");
            }
        }
    }
}
