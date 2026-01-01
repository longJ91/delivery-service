package jjh.delivery.domain.product;

/**
 * Product Status Enum
 */
public enum ProductStatus {

    /** 판매 대기 */
    DRAFT,

    /** 판매중 */
    ACTIVE,

    /** 품절 */
    OUT_OF_STOCK,

    /** 판매 중지 */
    INACTIVE,

    /** 삭제 */
    DELETED;

    /**
     * 판매 가능한 상태인지 확인
     */
    public boolean isSellable() {
        return this == ACTIVE;
    }

    /**
     * 노출 가능한 상태인지 확인
     */
    public boolean isVisible() {
        return this == ACTIVE || this == OUT_OF_STOCK;
    }

    /**
     * 수정 가능한 상태인지 확인
     */
    public boolean isEditable() {
        return this != DELETED;
    }
}
