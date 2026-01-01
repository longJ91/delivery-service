package jjh.delivery.application.port.in;

/**
 * Refresh Token Use Case - Driving Port (Inbound)
 * 토큰 갱신 유스케이스
 */
public interface RefreshTokenUseCase {

    /**
     * 토큰 갱신
     *
     * @param refreshToken 리프레시 토큰
     * @return 새로운 토큰 정보
     */
    LoginUseCase.TokenResult refresh(String refreshToken);
}
