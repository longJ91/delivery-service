package jjh.delivery.config.cache;

import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Entity ID based KeyGenerator
 *
 * 단일 엔티티 ID 조회용 캐시 키 생성기
 *
 * Key Pattern: {id}
 * Full Key: delivery::{cacheName}::{id}
 *
 * Usage:
 * @Cacheable(cacheNames = CacheNames.PRODUCTS, keyGenerator = CacheNames.ENTITY_KEY_GENERATOR)
 * Optional<Product> loadById(UUID productId);
 */
@Component(CacheNames.ENTITY_KEY_GENERATOR)
public class EntityKeyGenerator implements KeyGenerator {

    @Override
    public Object generate(Object target, Method method, Object... params) {
        if (params.length == 0) {
            return "all";
        }

        Object firstParam = params[0];

        // UUID 타입 처리
        if (firstParam instanceof UUID uuid) {
            return uuid.toString();
        }

        // String 타입 처리 (이미 문자열인 경우)
        if (firstParam instanceof String str) {
            return str;
        }

        // 기타 타입은 toString() 사용
        return firstParam.toString();
    }
}
