package jjh.delivery.application.port.out;

import java.util.UUID;

/**
 * Update Customer Credentials Port - Driven Port (Outbound)
 * 고객 인증 정보 업데이트를 위한 포트
 * jOOQ Adapter에서 구현 (컴파일 타임 타입 안전성)
 */
public interface UpdateCustomerCredentialsPort {

    void updatePassword(UUID customerId, String encodedPassword);
}
