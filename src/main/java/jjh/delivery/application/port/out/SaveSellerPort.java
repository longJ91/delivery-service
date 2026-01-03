package jjh.delivery.application.port.out;

import jjh.delivery.domain.seller.Seller;

import java.util.UUID;

/**
 * Save Seller Port - Driven Port (Outbound)
 * 판매자 저장 포트
 */
public interface SaveSellerPort {

    Seller save(Seller seller);

    void delete(UUID sellerId);
}
