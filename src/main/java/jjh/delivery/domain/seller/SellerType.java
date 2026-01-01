package jjh.delivery.domain.seller;

/**
 * Seller Type Enum
 */
public enum SellerType {

    /** 개인 사업자 */
    INDIVIDUAL,

    /** 법인 사업자 */
    CORPORATION,

    /** 해외 판매자 */
    OVERSEAS;

    /**
     * 국내 판매자인지 확인
     */
    public boolean isDomestic() {
        return this != OVERSEAS;
    }
}
