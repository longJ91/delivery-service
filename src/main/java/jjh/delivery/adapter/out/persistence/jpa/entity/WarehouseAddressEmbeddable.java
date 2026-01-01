package jjh.delivery.adapter.out.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jjh.delivery.domain.seller.WarehouseAddress;

/**
 * Warehouse Address Embeddable for JPA
 */
@Embeddable
public class WarehouseAddressEmbeddable {

    @Column(name = "warehouse_postal_code", length = 10)
    private String postalCode;

    @Column(name = "warehouse_address1", length = 200)
    private String address1;

    @Column(name = "warehouse_address2", length = 200)
    private String address2;

    @Column(name = "warehouse_contact_name", length = 100)
    private String contactName;

    @Column(name = "warehouse_contact_phone", length = 20)
    private String contactPhone;

    protected WarehouseAddressEmbeddable() {
    }

    public WarehouseAddressEmbeddable(
            String postalCode,
            String address1,
            String address2,
            String contactName,
            String contactPhone
    ) {
        this.postalCode = postalCode;
        this.address1 = address1;
        this.address2 = address2;
        this.contactName = contactName;
        this.contactPhone = contactPhone;
    }

    /**
     * Convert from domain Value Object
     */
    public static WarehouseAddressEmbeddable fromDomain(WarehouseAddress warehouseAddress) {
        if (warehouseAddress == null) {
            return null;
        }
        return new WarehouseAddressEmbeddable(
                warehouseAddress.postalCode(),
                warehouseAddress.address1(),
                warehouseAddress.address2(),
                warehouseAddress.contactName(),
                warehouseAddress.contactPhone()
        );
    }

    /**
     * Convert to domain Value Object
     */
    public WarehouseAddress toDomain() {
        if (postalCode == null) {
            return null;
        }
        return new WarehouseAddress(
                postalCode,
                address1,
                address2,
                contactName,
                contactPhone
        );
    }

    // Getters
    public String getPostalCode() {
        return postalCode;
    }

    public String getAddress1() {
        return address1;
    }

    public String getAddress2() {
        return address2;
    }

    public String getContactName() {
        return contactName;
    }

    public String getContactPhone() {
        return contactPhone;
    }
}
