package jjh.delivery.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI (Swagger) Configuration
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private int serverPort;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Local Development Server"),
                        new Server()
                                .url("https://api.delivery.com")
                                .description("Production Server")
                ));
    }

    private Info apiInfo() {
        return new Info()
                .title("Delivery Service API")
                .description("""
                        배달 서비스 백엔드 API 명세서

                        ## Architecture
                        - Hexagonal Architecture (Ports & Adapters)
                        - Spring Boot 4.0.1 / Java 21

                        ## Features
                        - 주문 생성 및 관리
                        - 주문 상태 추적
                        - 고객/가게별 주문 조회
                        """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("Delivery Service Team"))
                .license(new License()
                        .name("MIT"));
    }
}
