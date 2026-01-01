package jjh.delivery.adapter.out.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jjh.delivery.domain.order.ShippingAddress;

/**
 * Shipping Address Embeddable for JPA
 */
@Embeddable
public class ShippingAddressEmbeddable {

    @Column(name = "recipient_name", nullable = false, length = 100)
    private String recipientName;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "postal_code", nullable = false, length = 10)
    private String postalCode;

    @Column(name = "address1", nullable = false, length = 200)
    private String address1;

    @Column(name = "address2", length = 200)
    private String address2;

    @Column(name = "delivery_note", length = 500)
    private String deliveryNote;

    protected ShippingAddressEmbeddable() {
    }

    public ShippingAddressEmbeddable(
            String recipientName,
            String phoneNumber,
            String postalCode,
            String address1,
            String address2,
            String deliveryNote
    ) {
        this.recipientName = recipientName;
        this.phoneNumber = phoneNumber;
        this.postalCode = postalCode;
        this.address1 = address1;
        this.address2 = address2;
        this.deliveryNote = deliveryNote;
    }

    /**
     * Convert from domain Value Object
     */
    public static ShippingAddressEmbeddable fromDomain(ShippingAddress shippingAddress) {
        if (shippingAddress == null) {
            return null;
        }
        return new ShippingAddressEmbeddable(
                shippingAddress.recipientName(),
                shippingAddress.phoneNumber(),
                shippingAddress.postalCode(),
                shippingAddress.address1(),
                shippingAddress.address2(),
                shippingAddress.deliveryNote()
        );
    }

    /**
     * Convert to domain Value Object
     */
    public ShippingAddress toDomain() {
        return new ShippingAddress(
                recipientName,
                phoneNumber,
                postalCode,
                address1,
                address2,
                deliveryNote
        );
    }

    // Getters
    public String getRecipientName() {
        return recipientName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getAddress1() {
        return address1;
    }

    public String getAddress2() {
        return address2;
    }

    public String getDeliveryNote() {
        return deliveryNote;
    }
}
