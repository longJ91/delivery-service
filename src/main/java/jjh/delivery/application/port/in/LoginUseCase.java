package jjh.delivery.application.port.in;

/**
 * Login Use Case - Driving Port (Inbound)
 * 로그인 유스케이스
 */
public interface LoginUseCase {

    /**
     * 로그인
     *
     * @param command 로그인 정보
     * @return 토큰 정보
     */
    TokenResult login(LoginCommand command);

    record LoginCommand(
            String email,
            String password
    ) {
        public LoginCommand {
            if (email == null || email.isBlank()) {
                throw new IllegalArgumentException("Email is required");
            }
            if (password == null || password.isBlank()) {
                throw new IllegalArgumentException("Password is required");
            }
        }
    }

    record TokenResult(
            String accessToken,
            String refreshToken,
            long expiresIn
    ) {}
}
