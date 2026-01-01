package jjh.delivery.application.port.out;

import jjh.delivery.domain.seller.Seller;

/**
 * Save Seller Port - Driven Port (Outbound)
 * 판매자 저장 포트
 */
public interface SaveSellerPort {

    Seller save(Seller seller);

    void delete(String sellerId);
}
