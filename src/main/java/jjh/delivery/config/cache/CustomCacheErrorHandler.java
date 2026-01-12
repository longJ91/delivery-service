package jjh.delivery.config.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;

/**
 * Custom Cache Error Handler for graceful degradation
 *
 * Redis 연결 실패 시에도 애플리케이션이 정상 동작하도록 함
 * - 캐시 오류 발생 시 로그만 남기고 원본 데이터 소스 조회
 * - Circuit breaker 패턴 적용 가능
 */
public class CustomCacheErrorHandler implements CacheErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(CustomCacheErrorHandler.class);

    @Override
    public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
        log.warn("Cache GET failed - cache: {}, key: {}, error: {}",
                cache.getName(), key, exception.getMessage());
        // 캐시 조회 실패 시 원본 데이터 소스에서 조회하도록 예외를 삼킴
    }

    @Override
    public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
        log.warn("Cache PUT failed - cache: {}, key: {}, error: {}",
                cache.getName(), key, exception.getMessage());
        // 캐시 저장 실패 시 무시 (데이터는 이미 DB에 있음)
    }

    @Override
    public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
        log.warn("Cache EVICT failed - cache: {}, key: {}, error: {}",
                cache.getName(), key, exception.getMessage());
        // 캐시 삭제 실패 시 무시 (TTL로 자동 만료됨)
    }

    @Override
    public void handleCacheClearError(RuntimeException exception, Cache cache) {
        log.warn("Cache CLEAR failed - cache: {}, error: {}",
                cache.getName(), exception.getMessage());
        // 캐시 전체 삭제 실패 시 무시
    }
}
