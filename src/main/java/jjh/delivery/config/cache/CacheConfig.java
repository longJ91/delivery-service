package jjh.delivery.config.cache;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jjh.delivery.config.cache.mixin.CategoryMixin;
import jjh.delivery.config.cache.mixin.ProductMixin;
import jjh.delivery.config.cache.mixin.SellerMixin;
import jjh.delivery.domain.category.Category;
import jjh.delivery.domain.product.Product;
import jjh.delivery.domain.seller.Seller;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.DefaultTyping;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

import java.util.HashMap;
import java.util.Map;

/**
 * Redis Cache Configuration (Jackson 3)
 *
 * TTL Strategy:
 * - categories: 24h (마스터 데이터, 거의 변경되지 않음)
 * - sellers: 2h (변경이 드묾)
 * - products: 1h (조회 빈도 높음, 간헐적 변경)
 * - sellerInfo: 30m (주문 생성 시 빈번 조회)
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Redis CacheManager with per-cache TTL configuration
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Default configuration
        RedisCacheConfiguration defaultConfig = createDefaultCacheConfiguration();

        // Per-cache TTL configuration
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        cacheConfigurations.put(CacheNames.CATEGORIES, defaultConfig.entryTtl(CacheNames.CATEGORIES_TTL));
        cacheConfigurations.put(CacheNames.SELLERS, defaultConfig.entryTtl(CacheNames.SELLERS_TTL));
        cacheConfigurations.put(CacheNames.PRODUCTS, defaultConfig.entryTtl(CacheNames.PRODUCTS_TTL));
        cacheConfigurations.put(CacheNames.SELLER_INFO, defaultConfig.entryTtl(CacheNames.SELLER_INFO_TTL));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }

    /**
     * Default Redis cache configuration with Jackson 3 JSON serialization
     */
    private RedisCacheConfiguration createDefaultCacheConfiguration() {
        JsonMapper jsonMapper = createCacheJsonMapper();

        // Jackson 3 기반 직렬화기
        GenericJacksonJsonRedisSerializer jsonSerializer =
                new GenericJacksonJsonRedisSerializer(jsonMapper);

        return RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer))
                .disableCachingNullValues()
                .prefixCacheNameWith("delivery::");
    }

    /**
     * JsonMapper configured for Redis cache serialization (Jackson 3)
     *
     * Jackson 3 변경사항:
     * - ObjectMapper → JsonMapper (Builder 패턴 필수)
     * - LaissezFaireSubTypeValidator → BasicPolymorphicTypeValidator (화이트리스트 기반)
     * - JavaTimeModule 내장 (별도 등록 불필요)
     * - WRITE_DATES_AS_TIMESTAMPS 기본 disabled
     */
    private JsonMapper createCacheJsonMapper() {
        // 다형성 타입 검증기 (화이트리스트 기반 보안 강화)
        BasicPolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class)
                .allowIfSubType("jjh.delivery.")
                .allowIfSubType("java.util.")
                .allowIfSubType("java.time.")
                .allowIfSubType("java.lang.")
                .allowIfSubType("java.math.")
                .build();

        return JsonMapper.builder()
                // Field visibility 설정 (private 필드 직접 접근)
                .changeDefaultVisibility(vc -> vc
                        .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                        .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                        .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE))

                // 다형성 타입 정보 활성화 (역직렬화 시 타입 복원용)
                .activateDefaultTypingAsProperty(
                        typeValidator,
                        DefaultTyping.NON_FINAL,
                        JsonTypeInfo.Id.CLASS.getDefaultPropertyName())

                // 역직렬화 설정 (알 수 없는 속성 무시 - 버전 호환성)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

                // Mixin 등록 (Builder 패턴 도메인 객체 역직렬화 지원)
                .addMixIn(Product.class, ProductMixin.class)
                .addMixIn(Product.Builder.class, ProductMixin.BuilderMixin.class)
                .addMixIn(Seller.class, SellerMixin.class)
                .addMixIn(Seller.Builder.class, SellerMixin.BuilderMixin.class)
                .addMixIn(Category.class, CategoryMixin.class)
                .addMixIn(Category.Builder.class, CategoryMixin.BuilderMixin.class)
                .build();
    }

    /**
     * Custom CacheErrorHandler for graceful degradation
     */
    @Bean
    public CacheErrorHandler cacheErrorHandler() {
        return new CustomCacheErrorHandler();
    }
}
