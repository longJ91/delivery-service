package jjh.delivery.application.port.in;

import jjh.delivery.domain.customer.Customer;

import java.util.UUID;

/**
 * Get My Profile Use Case - Driving Port (Inbound)
 * 내 프로필 조회 유스케이스
 */
public interface GetMyProfileUseCase {

    /**
     * 내 프로필 조회
     *
     * @param customerId 고객 ID
     * @return 고객 정보
     */
    Customer getMyProfile(UUID customerId);
}
