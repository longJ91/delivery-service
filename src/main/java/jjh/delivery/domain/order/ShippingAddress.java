package jjh.delivery.domain.order;

/**
 * Shipping Address Value Object (Snapshot)
 *
 * 주문 시점의 배송 주소를 스냅샷으로 저장.
 * 고객의 주소가 변경되어도 주문 당시 주소는 유지됨.
 */
public record ShippingAddress(
        String recipientName,
        String phoneNumber,
        String postalCode,
        String address1,
        String address2,
        String deliveryNote
) {
    public ShippingAddress {
        if (recipientName == null || recipientName.isBlank()) {
            throw new IllegalArgumentException("recipientName is required");
        }
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("phoneNumber is required");
        }
        if (postalCode == null || postalCode.isBlank()) {
            throw new IllegalArgumentException("postalCode is required");
        }
        if (address1 == null || address1.isBlank()) {
            throw new IllegalArgumentException("address1 is required");
        }
    }

    /**
     * Factory method
     */
    public static ShippingAddress of(
            String recipientName,
            String phoneNumber,
            String postalCode,
            String address1,
            String address2,
            String deliveryNote
    ) {
        return new ShippingAddress(recipientName, phoneNumber, postalCode, address1, address2, deliveryNote);
    }

    /**
     * Simplified factory method
     */
    public static ShippingAddress of(
            String recipientName,
            String phoneNumber,
            String postalCode,
            String address1
    ) {
        return new ShippingAddress(recipientName, phoneNumber, postalCode, address1, null, null);
    }

    /**
     * 전체 주소 문자열 반환
     */
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        sb.append("(").append(postalCode).append(") ");
        sb.append(address1);
        if (address2 != null && !address2.isBlank()) {
            sb.append(" ").append(address2);
        }
        return sb.toString();
    }
}
