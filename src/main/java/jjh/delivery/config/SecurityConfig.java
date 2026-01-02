package jjh.delivery.config;

import jjh.delivery.config.security.JwtAuthenticationFilter;
import jjh.delivery.config.security.JwtProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 설정
 * JWT 기반 Stateless 인증
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(JwtProperties.class)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/api/v2/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v2/categories/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v2/products/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v2/sellers/{sellerId}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v2/sellers/{sellerId}/products").permitAll()
                        // Webhook endpoints (signature verification in controller)
                        .requestMatchers("/api/v2/webhooks/**").permitAll()
                        // Swagger/OpenAPI
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/api-docs/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        // Actuator
                        .requestMatchers("/actuator/**").permitAll()
                        // Seller admin endpoints
                        .requestMatchers("/api/v2/sellers/me/**").hasRole("SELLER")
                        // All other endpoints require authentication
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
