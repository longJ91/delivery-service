package jjh.delivery.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

/**
 * Jackson 3 JsonMapper Configuration
 *
 * Jackson 3 기본 설정:
 * - java.time 타입 지원 (내장)
 * - WRITE_DATES_AS_TIMESTAMPS 기본 disabled
 * - SORT_PROPERTIES_ALPHABETICALLY 기본 enabled
 */
@Configuration
public class JacksonConfig {

    @Bean
    public JsonMapper jsonMapper() {
        return JsonMapper.builder()
                // Jackson 3에서는 JavaTimeModule이 내장되어 있어 별도 등록 불필요
                // WRITE_DATES_AS_TIMESTAMPS가 기본 disabled이므로 ISO-8601 형식 자동 적용
                .build();
    }
}
