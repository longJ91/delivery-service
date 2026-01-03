package jjh.delivery.application.service;

import lombok.RequiredArgsConstructor;

import jjh.delivery.application.port.in.GetMyProfileUseCase;
import jjh.delivery.application.port.in.LoginUseCase;
import jjh.delivery.application.port.in.RefreshTokenUseCase;
import jjh.delivery.application.port.in.RegisterUseCase;
import jjh.delivery.application.port.out.LoadCustomerCredentialsPort;
import jjh.delivery.application.port.out.LoadCustomerPort;
import jjh.delivery.application.port.out.SaveCustomerPort;
import jjh.delivery.config.security.JwtTokenProvider;
import jjh.delivery.domain.customer.Customer;
import jjh.delivery.domain.customer.exception.CustomerNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Auth Service
 * 인증 관련 유스케이스 구현
 */
@Service
@Transactional
@RequiredArgsConstructor
public class AuthService implements RegisterUseCase, LoginUseCase, RefreshTokenUseCase, GetMyProfileUseCase {

    private final LoadCustomerPort loadCustomerPort;
    private final SaveCustomerPort saveCustomerPort;
    private final LoadCustomerCredentialsPort loadCustomerCredentialsPort;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Customer register(RegisterCommand command) {
        // 이메일 중복 검사
        if (loadCustomerPort.existsByEmail(command.email())) {
            throw new IllegalArgumentException("Email already exists: " + command.email());
        }

        // 도메인 객체 생성
        Customer customer = Customer.builder()
                .email(command.email())
                .name(command.name())
                .phoneNumber(command.phone())
                .build();

        // 비밀번호 암호화 후 저장
        String encodedPassword = passwordEncoder.encode(command.password());
        return saveCustomerPort.saveWithPassword(customer, encodedPassword);
    }

    @Override
    @Transactional(readOnly = true)
    public TokenResult login(LoginCommand command) {
        // 이메일로 고객 조회
        Customer customer = loadCustomerPort.findByEmail(command.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        // 비밀번호 검증
        String storedPassword = loadCustomerCredentialsPort.findPasswordByEmail(command.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(command.password(), storedPassword)) {
            throw new BadCredentialsException("Invalid email or password");
        }

        // 고객 상태 검증
        if (!customer.getStatus().isActive()) {
            throw new IllegalStateException("Customer account is not active: " + customer.getStatus());
        }

        // 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(
                customer.getId().toString(),
                customer.getEmail(),
                "CUSTOMER"
        );
        String refreshToken = jwtTokenProvider.createRefreshToken(customer.getId().toString());

        return new TokenResult(
                accessToken,
                refreshToken,
                jwtTokenProvider.getAccessTokenExpiration()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public TokenResult refresh(String refreshToken) {
        // 토큰 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        // 사용자 ID 추출
        String customerIdStr = jwtTokenProvider.getUserId(refreshToken);
        UUID customerId = UUID.fromString(customerIdStr);

        // 고객 조회
        Customer customer = loadCustomerPort.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerIdStr));

        // 고객 상태 검증
        if (!customer.getStatus().isActive()) {
            throw new IllegalStateException("Customer account is not active: " + customer.getStatus());
        }

        // 새 토큰 생성
        String newAccessToken = jwtTokenProvider.createAccessToken(
                customer.getId().toString(),
                customer.getEmail(),
                "CUSTOMER"
        );
        String newRefreshToken = jwtTokenProvider.createRefreshToken(customer.getId().toString());

        return new TokenResult(
                newAccessToken,
                newRefreshToken,
                jwtTokenProvider.getAccessTokenExpiration()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Customer getMyProfile(UUID customerId) {
        return loadCustomerPort.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId.toString()));
    }
}
