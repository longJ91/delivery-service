package jjh.delivery.domain.returns;

/**
 * Return Reason Enum
 */
public enum ReturnReason {

    /** 단순 변심 */
    CHANGE_OF_MIND("단순 변심", false),

    /** 상품 불량 */
    DEFECTIVE("상품 불량", true),

    /** 오배송 */
    WRONG_ITEM("오배송", true),

    /** 상품 파손 */
    DAMAGED("상품 파손", true),

    /** 상품 설명과 다름 */
    NOT_AS_DESCRIBED("상품 설명과 다름", true),

    /** 배송 지연 */
    DELAYED_DELIVERY("배송 지연", false),

    /** 기타 */
    OTHER("기타", false);

    private final String displayName;
    private final boolean isSellerFault;

    ReturnReason(String displayName, boolean isSellerFault) {
        this.displayName = displayName;
        this.isSellerFault = isSellerFault;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * 판매자 귀책 사유인지 확인
     */
    public boolean isSellerFault() {
        return isSellerFault;
    }

    /**
     * 반품 배송비 부담 주체 (true: 판매자, false: 구매자)
     */
    public boolean isSellerPaysShipping() {
        return isSellerFault;
    }
}
