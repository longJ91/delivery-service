package jjh.delivery.adapter.in.web.auth;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import jjh.delivery.adapter.in.web.auth.dto.*;
import jjh.delivery.application.port.in.GetMyProfileUseCase;
import jjh.delivery.application.port.in.LoginUseCase;
import jjh.delivery.application.port.in.RefreshTokenUseCase;
import jjh.delivery.application.port.in.RegisterUseCase;
import jjh.delivery.config.security.AuthenticatedUser;
import jjh.delivery.domain.customer.Customer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Auth Controller
 * 인증 관련 API 엔드포인트
 */
@RestController
@RequestMapping("/api/v2/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RegisterUseCase registerUseCase;
    private final LoginUseCase loginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final GetMyProfileUseCase getMyProfileUseCase;

    /**
     * 회원가입
     * POST /api/v2/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<CustomerResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        RegisterUseCase.RegisterCommand command = new RegisterUseCase.RegisterCommand(
                request.email(),
                request.password(),
                request.name(),
                request.phone()
        );

        Customer customer = registerUseCase.register(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CustomerResponse.from(customer));
    }

    /**
     * 로그인
     * POST /api/v2/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        LoginUseCase.LoginCommand command = new LoginUseCase.LoginCommand(
                request.email(),
                request.password()
        );

        LoginUseCase.TokenResult result = loginUseCase.login(command);
        return ResponseEntity.ok(TokenResponse.of(
                result.accessToken(),
                result.refreshToken(),
                result.expiresIn()
        ));
    }

    /**
     * 토큰 갱신
     * POST /api/v2/auth/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        LoginUseCase.TokenResult result = refreshTokenUseCase.refresh(request.refreshToken());
        return ResponseEntity.ok(TokenResponse.of(
                result.accessToken(),
                result.refreshToken(),
                result.expiresIn()
        ));
    }

    /**
     * 내 정보 조회
     * GET /api/v2/auth/me
     */
    @GetMapping("/me")
    public ResponseEntity<CustomerResponse> getMe(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        Customer customer = getMyProfileUseCase.getMyProfile(user.id());
        return ResponseEntity.ok(CustomerResponse.from(customer));
    }
}
