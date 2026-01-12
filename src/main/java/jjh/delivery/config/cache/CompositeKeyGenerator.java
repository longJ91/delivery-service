package jjh.delivery.config.cache;

import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Composite KeyGenerator for multi-parameter queries
 *
 * 복합 파라미터 조회용 캐시 키 생성기
 *
 * Key Pattern: {param1}:{param2}:...:{paramN}
 * Full Key: delivery::{cacheName}::{param1}:{param2}:...:{paramN}
 *
 * Usage:
 * @Cacheable(cacheNames = CacheNames.PRODUCTS, keyGenerator = CacheNames.COMPOSITE_KEY_GENERATOR)
 * List<Product> findBySellerIdAndStatus(UUID sellerId, ProductStatus status);
 */
@Component(CacheNames.COMPOSITE_KEY_GENERATOR)
public class CompositeKeyGenerator implements KeyGenerator {

    private static final String SEPARATOR = ":";
    private static final String NULL_VALUE = "null";

    @Override
    public Object generate(Object target, Method method, Object... params) {
        if (params.length == 0) {
            return method.getName();
        }

        if (params.length == 1) {
            return formatSingleParam(params[0]);
        }

        return Arrays.stream(params)
                .map(this::formatSingleParam)
                .collect(Collectors.joining(SEPARATOR));
    }

    private String formatSingleParam(Object param) {
        if (param == null) {
            return NULL_VALUE;
        }

        // UUID 타입 처리
        if (param instanceof UUID uuid) {
            return uuid.toString();
        }

        // Enum 타입 처리
        if (param instanceof Enum<?> enumValue) {
            return enumValue.name();
        }

        // Collection 타입 처리 (정렬된 해시값 사용)
        if (param instanceof Iterable<?> iterable) {
            return formatIterable(iterable);
        }

        // 배열 타입 처리
        if (param.getClass().isArray()) {
            return formatArray(param);
        }

        return param.toString();
    }

    private String formatIterable(Iterable<?> iterable) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Object item : iterable) {
            if (!first) {
                sb.append(",");
            }
            sb.append(formatSingleParam(item));
            first = false;
        }
        sb.append("]");
        return sb.toString();
    }

    private String formatArray(Object array) {
        if (array instanceof Object[] objArray) {
            return "[" + Arrays.stream(objArray)
                    .map(this::formatSingleParam)
                    .collect(Collectors.joining(",")) + "]";
        }

        // Primitive arrays
        return "[" + Objects.hashCode(array) + "]";
    }
}
