package jjh.delivery.adapter.in.web.seller.dto;

import jjh.delivery.domain.seller.Seller;
import jjh.delivery.domain.seller.SellerStatus;
import jjh.delivery.domain.seller.SellerType;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 판매자 응답
 */
public record SellerResponse(
        String id,
        String businessName,
        String businessNumber,
        String representativeName,
        String email,
        String phoneNumber,
        SellerType sellerType,
        SellerStatus status,
        WarehouseAddressResponse warehouseAddress,
        List<String> categoryIds,
        boolean canSell,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime approvedAt
) {
    public static SellerResponse from(Seller seller) {
        WarehouseAddressResponse warehouseAddress = null;
        if (seller.getWarehouseAddress() != null) {
            warehouseAddress = new WarehouseAddressResponse(
                    seller.getWarehouseAddress().postalCode(),
                    seller.getWarehouseAddress().address1(),
                    seller.getWarehouseAddress().address2(),
                    seller.getWarehouseAddress().contactName(),
                    seller.getWarehouseAddress().contactPhone(),
                    seller.getWarehouseAddress().getFullAddress()
            );
        }

        return new SellerResponse(
                seller.getId(),
                seller.getBusinessName(),
                seller.getBusinessNumber(),
                seller.getRepresentativeName(),
                seller.getEmail(),
                seller.getPhoneNumber(),
                seller.getSellerType(),
                seller.getStatus(),
                warehouseAddress,
                seller.getCategoryIds(),
                seller.canSell(),
                seller.getCreatedAt(),
                seller.getUpdatedAt(),
                seller.getApprovedAt()
        );
    }

    public record WarehouseAddressResponse(
            String postalCode,
            String address1,
            String address2,
            String contactName,
            String contactPhone,
            String fullAddress
    ) {
    }
}
