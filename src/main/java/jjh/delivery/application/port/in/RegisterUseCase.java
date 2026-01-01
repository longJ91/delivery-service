package jjh.delivery.application.port.in;

import jjh.delivery.domain.customer.Customer;

/**
 * Register Use Case - Driving Port (Inbound)
 * 회원가입 유스케이스
 */
public interface RegisterUseCase {

    /**
     * 회원가입
     *
     * @param command 회원가입 정보
     * @return 생성된 고객 정보
     */
    Customer register(RegisterCommand command);

    record RegisterCommand(
            String email,
            String password,
            String name,
            String phone
    ) {
        public RegisterCommand {
            if (email == null || email.isBlank()) {
                throw new IllegalArgumentException("Email is required");
            }
            if (password == null || password.isBlank()) {
                throw new IllegalArgumentException("Password is required");
            }
            if (password.length() < 8) {
                throw new IllegalArgumentException("Password must be at least 8 characters");
            }
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Name is required");
            }
            if (phone == null || phone.isBlank()) {
                throw new IllegalArgumentException("Phone is required");
            }
        }
    }
}
