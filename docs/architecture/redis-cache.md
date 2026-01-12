# Redis Cache Architecture

Spring Boot Cache Abstraction을 활용한 Redis 캐시 시스템 구현 가이드.

## 목차

1. [개요](#개요)
2. [동작 흐름도](#동작-흐름도)
3. [아키텍처 구성](#아키텍처-구성)
4. [설정 파일 상세](#설정-파일-상세)
5. [캐시 어노테이션 사용법](#캐시-어노테이션-사용법)
6. [TTL 전략](#ttl-전략)
7. [장애 대응](#장애-대응)

---

## 개요

### 목적
- DB 부하 감소: 반복적인 조회 쿼리를 Redis에서 처리
- 응답 속도 향상: 캐시 히트 시 DB 접근 없이 즉시 응답
- 확장성: 분산 캐시를 통한 수평 확장 지원

### 기술 스택
- Spring Boot 4.0
- Spring Data Redis 4.0
- Redis 7 (Alpine)
- Jackson 2 (JSON 직렬화)

---

## 동작 흐름도

### 1. 캐시 조회 흐름 (@Cacheable)

```
┌─────────────┐     ┌──────────────────┐     ┌─────────────┐     ┌──────────┐
│   Client    │────▶│  Cache Aspect    │────▶│    Redis    │     │    DB    │
│   Request   │     │  (AOP Proxy)     │     │   Server    │     │          │
└─────────────┘     └──────────────────┘     └─────────────┘     └──────────┘
                              │                      │                  │
                              │  1. Generate Key     │                  │
                              │  (KeyGenerator)      │                  │
                              │                      │                  │
                              │  2. Check Cache ─────▶                  │
                              │                      │                  │
                    ┌─────────┴─────────┐           │                  │
                    │                   │           │                  │
               Cache HIT           Cache MISS       │                  │
                    │                   │           │                  │
                    ▼                   │           │                  │
            ┌───────────────┐          │           │                  │
            │ Deserialize   │          │           │                  │
            │ JSON → Object │          │           │                  │
            └───────────────┘          │           │                  │
                    │                   │           │                  │
                    │                   │  3. Call Method ────────────▶
                    │                   │                              │
                    │                   │  4. Get Result ◀─────────────
                    │                   │           │                  │
                    │                   │  5. Serialize & Cache ──────▶
                    │                   │           │                  │
                    ▼                   ▼           │                  │
            ┌───────────────────────────────┐      │                  │
            │       Return Response         │      │                  │
            └───────────────────────────────┘      │                  │
```

### 2. 캐시 무효화 흐름 (@CacheEvict)

```
┌─────────────┐     ┌──────────────────┐     ┌─────────────┐     ┌──────────┐
│   Client    │────▶│  Cache Aspect    │────▶│    Redis    │     │    DB    │
│  (Update)   │     │  (AOP Proxy)     │     │   Server    │     │          │
└─────────────┘     └──────────────────┘     └─────────────┘     └──────────┘
                              │                      │                  │
                              │  1. Execute Method ─────────────────────▶
                              │                      │                  │
                              │  2. Generate Key     │                  │
                              │  (KeyGenerator)      │                  │
                              │                      │                  │
                              │  3. Delete Cache ────▶                  │
                              │     (DEL key)        │                  │
                              │                      │                  │
                              ▼                      │                  │
                    ┌───────────────┐               │                  │
                    │    Return     │               │                  │
                    └───────────────┘               │                  │
```

### 3. 캐시 키 생성 과정

```
┌──────────────────────────────────────────────────────────────────────┐
│                        Cache Key Generation                          │
├──────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  Method Call: findById(UUID productId)                              │
│       │                                                              │
│       ▼                                                              │
│  ┌─────────────────────────────────────────────────────────────┐    │
│  │ KeyGenerator.generate(target, method, params)                │    │
│  │                                                              │    │
│  │  EntityKeyGenerator:                                         │    │
│  │    params[0] = UUID("550e8400-...")                         │    │
│  │    return "550e8400-e29b-41d4-a716-446655440000"            │    │
│  │                                                              │    │
│  │  CompositeKeyGenerator (다중 파라미터):                       │    │
│  │    params[0] = UUID("seller-id")                            │    │
│  │    params[1] = ProductStatus.ACTIVE                         │    │
│  │    return "seller-id:ACTIVE"                                │    │
│  └─────────────────────────────────────────────────────────────┘    │
│       │                                                              │
│       ▼                                                              │
│  ┌─────────────────────────────────────────────────────────────┐    │
│  │ RedisCacheConfiguration.prefixCacheNameWith("delivery::")   │    │
│  │                                                              │    │
│  │ Final Key: "delivery::products::550e8400-e29b-..."          │    │
│  └─────────────────────────────────────────────────────────────┘    │
│                                                                      │
└──────────────────────────────────────────────────────────────────────┘
```

---

## 아키텍처 구성

### 파일 구조

```
src/main/java/jjh/delivery/config/cache/
├── CacheConfig.java              # 메인 캐시 설정
├── CacheNames.java               # 캐시 이름 및 TTL 상수
├── CustomCacheErrorHandler.java  # 에러 핸들러 (Graceful Degradation)
├── EntityKeyGenerator.java       # 단일 ID 키 생성기
├── CompositeKeyGenerator.java    # 복합 키 생성기
└── mixin/
    ├── ProductMixin.java         # Product 직렬화 설정
    ├── SellerMixin.java          # Seller 직렬화 설정
    └── CategoryMixin.java        # Category 직렬화 설정
```

### 컴포넌트 관계도

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              Spring Context                                  │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌───────────────────┐                                                      │
│  │   @EnableCaching  │                                                      │
│  │   CacheConfig     │                                                      │
│  └─────────┬─────────┘                                                      │
│            │                                                                │
│            │ creates                                                        │
│            ▼                                                                │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                        RedisCacheManager                             │   │
│  │  ┌─────────────────────────────────────────────────────────────┐   │   │
│  │  │              RedisCacheConfiguration (default)               │   │   │
│  │  │  - StringRedisSerializer (keys)                              │   │   │
│  │  │  - GenericJackson2JsonRedisSerializer (values)              │   │   │
│  │  │  - prefix: "delivery::"                                      │   │   │
│  │  │  - disableCachingNullValues                                  │   │   │
│  │  └─────────────────────────────────────────────────────────────┘   │   │
│  │                                                                     │   │
│  │  ┌─────────────────────────────────────────────────────────────┐   │   │
│  │  │              Per-Cache TTL Configurations                    │   │   │
│  │  │  - categories: 24 hours                                      │   │   │
│  │  │  - sellers: 2 hours                                          │   │   │
│  │  │  - products: 1 hour                                          │   │   │
│  │  │  - sellerInfo: 30 minutes                                    │   │   │
│  │  └─────────────────────────────────────────────────────────────┘   │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│            │                                                                │
│            │ uses                                                           │
│            ▼                                                                │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                      RedisConnectionFactory                          │   │
│  │                    (Lettuce Connection Pool)                         │   │
│  │  - host: localhost:6379                                              │   │
│  │  - pool: max-active=8, max-idle=8, min-idle=2                       │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│            │                                                                │
│            ▼                                                                │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                           Redis Server                               │   │
│  │                      (redis:7-alpine + AOF)                          │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
│  ┌─────────────────┐  ┌─────────────────────┐  ┌─────────────────────┐    │
│  │ EntityKey       │  │ CompositeKey        │  │ CustomCacheError    │    │
│  │ Generator       │  │ Generator           │  │ Handler             │    │
│  └─────────────────┘  └─────────────────────┘  └─────────────────────┘    │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 설정 파일 상세

### 1. CacheConfig.java

메인 캐시 설정 클래스. `@EnableCaching`으로 캐시 기능을 활성화하고 `CacheManager` 빈을 생성.

```java
@Configuration
@EnableCaching
public class CacheConfig {
    // ...
}
```

#### 주요 구성요소

| 구성요소 | 역할 |
|---------|------|
| `@EnableCaching` | Spring Cache Abstraction 활성화, AOP 프록시 생성 |
| `CacheManager` | 캐시 인스턴스 관리, 캐시별 TTL 설정 |
| `RedisCacheConfiguration` | 직렬화 방식, 키 프리픽스, null 값 처리 정책 |
| `ObjectMapper` | JSON 직렬화/역직렬화 설정, Mixin 등록 |
| `CacheErrorHandler` | 캐시 오류 발생 시 fallback 처리 |

#### ObjectMapper 설정 상세

```java
private ObjectMapper createCacheObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();

    // 1. Java 8 Date/Time 지원
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    // 2. private 필드 직접 접근 (getter 불필요)
    objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
    objectMapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);

    // 3. 타입 정보 포함 (다형성 역직렬화 지원)
    objectMapper.activateDefaultTyping(
        LaissezFaireSubTypeValidator.instance,
        ObjectMapper.DefaultTyping.NON_FINAL,
        JsonTypeInfo.As.PROPERTY
    );

    // 4. 알 수 없는 속성 무시 (버전 호환성)
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    // 5. Builder 패턴 도메인 객체 Mixin 등록
    objectMapper.addMixIn(Product.class, ProductMixin.class);
    objectMapper.addMixIn(Product.Builder.class, ProductMixin.BuilderMixin.class);
    // ... (Seller, Category 동일)

    return objectMapper;
}
```

### 2. CacheNames.java

캐시 이름과 TTL을 상수로 관리. 하드코딩 방지 및 중앙 집중 관리.

```java
public final class CacheNames {
    // 캐시 이름 상수
    public static final String PRODUCTS = "products";
    public static final String SELLERS = "sellers";
    public static final String CATEGORIES = "categories";
    public static final String SELLER_INFO = "sellerInfo";

    // TTL 상수
    public static final Duration PRODUCTS_TTL = Duration.ofHours(1);
    public static final Duration SELLERS_TTL = Duration.ofHours(2);
    public static final Duration CATEGORIES_TTL = Duration.ofHours(24);
    public static final Duration SELLER_INFO_TTL = Duration.ofMinutes(30);

    // KeyGenerator 이름
    public static final String ENTITY_KEY_GENERATOR = "entityKeyGenerator";
    public static final String COMPOSITE_KEY_GENERATOR = "compositeKeyGenerator";
}
```

### 3. EntityKeyGenerator.java

단일 ID 파라미터용 캐시 키 생성기.

```java
@Component(CacheNames.ENTITY_KEY_GENERATOR)
public class EntityKeyGenerator implements KeyGenerator {

    @Override
    public Object generate(Object target, Method method, Object... params) {
        if (params.length == 0) {
            return "all";
        }

        Object firstParam = params[0];

        if (firstParam instanceof UUID uuid) {
            return uuid.toString();
        }

        return firstParam.toString();
    }
}
```

**생성 예시:**
- `findById(UUID.fromString("550e8400-..."))` → `"550e8400-e29b-41d4-..."`
- `findAll()` → `"all"`

### 4. CompositeKeyGenerator.java

복합 파라미터용 캐시 키 생성기. 여러 파라미터를 `:` 구분자로 연결.

```java
@Component(CacheNames.COMPOSITE_KEY_GENERATOR)
public class CompositeKeyGenerator implements KeyGenerator {

    @Override
    public Object generate(Object target, Method method, Object... params) {
        return Arrays.stream(params)
            .map(this::formatSingleParam)
            .collect(Collectors.joining(":"));
    }

    private String formatSingleParam(Object param) {
        if (param instanceof UUID uuid) return uuid.toString();
        if (param instanceof Enum<?> e) return e.name();
        if (param instanceof Iterable<?> i) return formatIterable(i);
        return param.toString();
    }
}
```

**생성 예시:**
- `findBySellerIdAndStatus(sellerId, ACTIVE)` → `"seller-uuid:ACTIVE"`
- `findByIds([id1, id2])` → `"[id1,id2]"`

### 5. CustomCacheErrorHandler.java

Redis 장애 시 Graceful Degradation 처리.

```java
public class CustomCacheErrorHandler implements CacheErrorHandler {

    @Override
    public void handleCacheGetError(RuntimeException ex, Cache cache, Object key) {
        log.warn("Cache GET failed - cache: {}, key: {}", cache.getName(), key);
        // 예외를 삼켜서 원본 메서드가 실행되도록 함
    }

    @Override
    public void handleCachePutError(RuntimeException ex, Cache cache, Object key, Object value) {
        log.warn("Cache PUT failed - cache: {}, key: {}", cache.getName(), key);
        // 저장 실패해도 데이터는 DB에 있으므로 무시
    }

    @Override
    public void handleCacheEvictError(RuntimeException ex, Cache cache, Object key) {
        log.warn("Cache EVICT failed - cache: {}, key: {}", cache.getName(), key);
        // TTL로 자연 만료되므로 무시
    }
}
```

### 6. Jackson Mixin (ProductMixin.java)

도메인 객체의 순수성을 유지하면서 Jackson 역직렬화 지원.

```java
@JsonDeserialize(builder = Product.Builder.class)
public abstract class ProductMixin {

    @JsonPOJOBuilder(withPrefix = "")
    public static abstract class BuilderMixin {
    }
}
```

**역할:**
- `@JsonDeserialize(builder = ...)`: 역직렬화 시 Builder 사용 지시
- `@JsonPOJOBuilder(withPrefix = "")`: Builder 메서드 접두사 설정 (setXxx 대신 xxx)

---

## 캐시 어노테이션 사용법

### @Cacheable - 조회 캐시

```java
@Cacheable(
    cacheNames = CacheNames.PRODUCTS,           // 캐시 이름
    keyGenerator = CacheNames.ENTITY_KEY_GENERATOR  // 키 생성기
)
public Optional<Product> findById(UUID productId) {
    return repository.findByIdWithVariants(productId)
        .map(mapper::toDomain);
}
```

### @CacheEvict - 캐시 무효화

```java
// 저장 시 해당 엔티티 캐시 삭제
@CacheEvict(
    cacheNames = CacheNames.SELLERS,
    key = "#seller.id.toString()"
)
public Seller save(Seller seller) {
    return repository.save(mapper.toEntity(seller));
}

// 삭제 시 해당 엔티티 캐시 삭제
@CacheEvict(
    cacheNames = CacheNames.SELLERS,
    keyGenerator = CacheNames.ENTITY_KEY_GENERATOR
)
public void delete(UUID sellerId) {
    repository.deleteById(sellerId);
}
```

### 고정 키 캐시

```java
// 전체 카테고리 트리 캐시
@Cacheable(cacheNames = CacheNames.CATEGORIES, key = "'tree'")
public List<Category> findAllActiveAsTree() {
    // ...
}

// 루트 카테고리 캐시
@Cacheable(cacheNames = CacheNames.CATEGORIES, key = "'roots'")
public List<Category> findRootCategories() {
    // ...
}
```

---

## TTL 전략

### 캐시별 TTL 설정 기준

| 캐시 | TTL | 설정 이유 |
|------|-----|----------|
| `categories` | 24시간 | 마스터 데이터, 변경 빈도 매우 낮음 |
| `sellers` | 2시간 | 판매자 정보, 변경 빈도 낮음 |
| `products` | 1시간 | 상품 정보, 조회 빈도 높고 간헐적 변경 |
| `sellerInfo` | 30분 | 주문 생성 시 빈번 조회, 짧은 유효성 필요 |

### Redis 키 구조

```
delivery::{cacheName}::{key}
```

**실제 예시:**
```
delivery::products::550e8400-e29b-41d4-a716-446655440000
delivery::categories::tree
delivery::categories::roots
delivery::sellers::123e4567-e89b-12d3-a456-426614174000
```

---

## 장애 대응

### Redis 연결 실패 시나리오

```
┌─────────────────────────────────────────────────────────────────────┐
│                    Redis Failure Scenario                           │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌──────────┐    ┌─────────────┐    ┌─────────┐    ┌──────────┐   │
│  │ Request  │───▶│ Cache Proxy │───▶│  Redis  │───▶│  ERROR   │   │
│  └──────────┘    └─────────────┘    └─────────┘    └──────────┘   │
│                         │                                │         │
│                         │                                │         │
│                         ▼                                │         │
│               ┌─────────────────────┐                   │         │
│               │ CustomCacheError    │◀──────────────────┘         │
│               │ Handler             │                              │
│               │ - Log warning       │                              │
│               │ - Swallow exception │                              │
│               └─────────────────────┘                              │
│                         │                                          │
│                         ▼                                          │
│               ┌─────────────────────┐                              │
│               │ Execute Original    │                              │
│               │ Method (DB Query)   │                              │
│               └─────────────────────┘                              │
│                         │                                          │
│                         ▼                                          │
│               ┌─────────────────────┐                              │
│               │ Return Response     │                              │
│               │ (Graceful Fallback) │                              │
│               └─────────────────────┘                              │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

### 모니터링

Spring Actuator를 통한 캐시 상태 모니터링:

```yaml
# application.yaml
management:
  endpoints:
    web:
      exposure:
        include: health,caches
  health:
    redis:
      enabled: true
```

**Actuator 엔드포인트:**
- `GET /actuator/health` - Redis 연결 상태 확인
- `GET /actuator/caches` - 등록된 캐시 목록 조회

---

## 확장 가이드

### 새 캐시 추가 절차

1. `CacheNames.java`에 상수 추가:
```java
public static final String NEW_CACHE = "newCache";
public static final Duration NEW_CACHE_TTL = Duration.ofMinutes(15);
```

2. `CacheConfig.java`에 TTL 설정 추가:
```java
cacheConfigurations.put(CacheNames.NEW_CACHE,
    defaultConfig.entryTtl(CacheNames.NEW_CACHE_TTL));
```

3. Adapter에 어노테이션 적용:
```java
@Cacheable(cacheNames = CacheNames.NEW_CACHE,
           keyGenerator = CacheNames.ENTITY_KEY_GENERATOR)
public Optional<NewEntity> findById(UUID id) { ... }
```

### Jackson 3 마이그레이션 (TODO)

Spring Boot의 Jackson 3 완전 전환 시:
```java
// 변경 전 (Jackson 2)
GenericJackson2JsonRedisSerializer

// 변경 후 (Jackson 3)
GenericJacksonJsonRedisSerializer
```
