package jjh.delivery.adapter.in.web.auth.dto;

/**
 * 토큰 응답
 */
public record TokenResponse(
        String accessToken,
        String refreshToken,
        long expiresIn
) {
    public static TokenResponse of(String accessToken, String refreshToken, long expiresIn) {
        return new TokenResponse(accessToken, refreshToken, expiresIn);
    }
}
