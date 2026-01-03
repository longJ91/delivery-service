package jjh.delivery.domain.customer;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Customer Address Value Object
 */
public record CustomerAddress(
        UUID id,
        String name,
        String recipientName,
        String phoneNumber,
        String postalCode,
        String address1,
        String address2,
        boolean isDefault,
        LocalDateTime createdAt
) {
    public CustomerAddress {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Address name is required");
        }
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
     * Factory method for new address
     */
    public static CustomerAddress of(
            String name,
            String recipientName,
            String phoneNumber,
            String postalCode,
            String address1,
            String address2,
            boolean isDefault
    ) {
        return new CustomerAddress(
                UUID.randomUUID(),
                name,
                recipientName,
                phoneNumber,
                postalCode,
                address1,
                address2,
                isDefault,
                LocalDateTime.now()
        );
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

    /**
     * Create copy with default flag changed
     */
    public CustomerAddress withDefault(boolean isDefault) {
        return new CustomerAddress(
                id, name, recipientName, phoneNumber, postalCode, address1, address2, isDefault, createdAt
        );
    }
}
