package jjh.delivery.domain.shipment;

/**
 * Shipping Carrier Enum (국내 택배사)
 */
public enum ShippingCarrier {

    CJ_LOGISTICS("CJ대한통운", "04"),
    HANJIN("한진택배", "05"),
    LOTTE("롯데택배", "08"),
    LOGEN("로젠택배", "06"),
    POST_OFFICE("우체국택배", "01"),
    GS_POSTBOX("GS25편의점택배", "24"),
    CU_POST("CU편의점택배", "46"),
    EPOST("e-PARK(이마트)택배", "47"),
    KDEXP("경동택배", "23"),
    DAESIN("대신택배", "22"),
    ILYANG("일양로지스", "11"),
    HOMEPICK("홈픽", "54"),
    OTHER("기타", "99");

    private final String displayName;
    private final String code;

    ShippingCarrier(String displayName, String code) {
        this.displayName = displayName;
        this.code = code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCode() {
        return code;
    }

    /**
     * 코드로 택배사 찾기
     */
    public static ShippingCarrier fromCode(String code) {
        for (ShippingCarrier carrier : values()) {
            if (carrier.code.equals(code)) {
                return carrier;
            }
        }
        return OTHER;
    }

    /**
     * 운송장 조회 URL 생성
     */
    public String getTrackingUrl(String trackingNumber) {
        return switch (this) {
            case CJ_LOGISTICS -> "https://www.cjlogistics.com/ko/tool/parcel/tracking?gnbInvcNo=" + trackingNumber;
            case HANJIN -> "https://www.hanjin.com/kor/CMS/DeliveryMgr/WaybillResult.do?mession-uuid=&wblnumText2=" + trackingNumber;
            case LOTTE -> "https://www.lotteglogis.com/mobile/reservation/tracking/linkView?InvNo=" + trackingNumber;
            case LOGEN -> "https://www.ilogen.com/web/personal/trace/" + trackingNumber;
            case POST_OFFICE -> "https://service.epost.go.kr/trace.RetrieveDomRi498.postal?sid1=" + trackingNumber;
            default -> null;
        };
    }
}
