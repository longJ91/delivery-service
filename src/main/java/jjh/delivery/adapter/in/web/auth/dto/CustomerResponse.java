package jjh.delivery.adapter.in.web.auth.dto;

import jjh.delivery.domain.customer.Customer;
import jjh.delivery.domain.customer.CustomerStatus;

import java.time.LocalDateTime;

/**
 * 고객 정보 응답
 */
public record CustomerResponse(
        String id,
        String email,
        String name,
        String phone,
        String profileImageUrl,
        CustomerStatus status,
        long pointBalance,
        LocalDateTime createdAt
) {
    public static CustomerResponse from(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getEmail(),
                customer.getName(),
                customer.getPhoneNumber(),
                null, // profileImageUrl - future enhancement
                customer.getStatus(),
                0L, // pointBalance - future enhancement
                customer.getCreatedAt()
        );
    }
}
