package jjh.delivery.config.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * 인증된 사용자 정보
 * Controller에서 @AuthenticationPrincipal로 주입받아 사용
 *
 * 주의: getUsername()은 Spring Security 규약상 사용자 식별자를 반환해야 하며,
 * 이 구현에서는 id(UUID 문자열)를 반환합니다.
 * 이메일이 필요한 경우 email() 메서드를 사용하세요.
 */
public record AuthenticatedUser(
        String id,
        String email,
        String role
) implements UserDetails {

    /**
     * UUID 타입으로 사용자 ID 반환
     */
    public UUID getUuidId() {
        return UUID.fromString(id);
    }

    public boolean isCustomer() {
        return "CUSTOMER".equals(role);
    }

    public boolean isSeller() {
        return "SELLER".equals(role);
    }

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getPassword() {
        return null;
    }

    /**
     * Spring Security 규약: 사용자 식별자 반환
     * 이 구현에서는 id(UUID 문자열)를 반환합니다.
     * 이메일이 필요한 경우 email() 메서드를 사용하세요.
     */
    @Override
    public String getUsername() {
        return id;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
