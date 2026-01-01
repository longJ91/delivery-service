package jjh.delivery.domain.seller;

/**
 * Warehouse Address Value Object
 * 단일 창고 주소 (Seller당 1개)
 */
public record WarehouseAddress(
        String postalCode,
        String address1,
        String address2,
        String contactName,
        String contactPhone
) {
    public WarehouseAddress {
        if (postalCode == null || postalCode.isBlank()) {
            throw new IllegalArgumentException("postalCode is required");
        }
        if (address1 == null || address1.isBlank()) {
            throw new IllegalArgumentException("address1 is required");
        }
        if (contactName == null || contactName.isBlank()) {
            throw new IllegalArgumentException("contactName is required");
        }
        if (contactPhone == null || contactPhone.isBlank()) {
            throw new IllegalArgumentException("contactPhone is required");
        }
    }

    /**
     * Factory method
     */
    public static WarehouseAddress of(
            String postalCode,
            String address1,
            String address2,
            String contactName,
            String contactPhone
    ) {
        return new WarehouseAddress(postalCode, address1, address2, contactName, contactPhone);
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
