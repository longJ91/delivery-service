# Jackson 3 Migration Plan

Spring Boot 4.x에서 Jackson 3으로 전환하기 위한 체계적인 마이그레이션 가이드.

## 목차

1. [개요](#개요)
2. [현황 분석](#현황-분석)
3. [주요 변경사항](#주요-변경사항)
4. [마이그레이션 계획](#마이그레이션-계획)
5. [파일별 변경 상세](#파일별-변경-상세)
6. [검증 체크리스트](#검증-체크리스트)

---

## 개요

### 배경
- Spring Boot 4.x부터 Jackson 3이 기본 JSON 라이브러리로 채택
- Jackson 2 지원은 deprecated 예정 (향후 Spring Boot 4.x 릴리스에서 제거)
- Jackson 3은 Java 17+ 필수, 불변 설정 패턴, 향상된 성능 제공

### 참고 문서
- [Jackson 3 Migration Guide (Official)](https://github.com/FasterXML/jackson/blob/main/jackson3/MIGRATING_TO_JACKSON_3.md)
- [Spring Blog: Jackson 3 Support](https://spring.io/blog/2025/10/07/introducing-jackson-3-support-in-spring/)
- [OpenRewrite Migration Recipe](https://docs.openrewrite.org/recipes/java/jackson/upgradejackson_2_3)

---

## 현황 분석

### 영향받는 파일 (7개)

| 파일 | Jackson 사용 내용 | 영향도 |
|------|------------------|--------|
| `JacksonConfig.java` | ObjectMapper, JavaTimeModule, SerializationFeature | **HIGH** |
| `CacheConfig.java` | ObjectMapper, LaissezFaireSubTypeValidator, Mixin, GenericJackson2JsonRedisSerializer | **CRITICAL** |
| `ProductMixin.java` | @JsonDeserialize, @JsonPOJOBuilder | LOW |
| `SellerMixin.java` | @JsonDeserialize, @JsonPOJOBuilder | LOW |
| `CategoryMixin.java` | @JsonDeserialize, @JsonPOJOBuilder | LOW |
| `OrderOutboxAdapter.java` | ObjectMapper, JsonProcessingException | MEDIUM |
| `OrderControllerTest.java` | ObjectMapper (테스트) | MEDIUM |

### 현재 사용 패턴

```java
// 1. ObjectMapper 생성 (mutable)
ObjectMapper objectMapper = new ObjectMapper();

// 2. 모듈 등록
objectMapper.registerModule(new JavaTimeModule());

// 3. Feature 설정
objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

// 4. Visibility 설정
objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);

// 5. 타입 정보 활성화
objectMapper.activateDefaultTyping(
    LaissezFaireSubTypeValidator.instance,  // ❌ Jackson 3에서 제거됨
    ObjectMapper.DefaultTyping.NON_FINAL,
    JsonTypeInfo.As.PROPERTY
);

// 6. Mixin 등록
objectMapper.addMixIn(Product.class, ProductMixin.class);
```

---

## 주요 변경사항

### 1. 패키지 변경

| Jackson 2 | Jackson 3 | 비고 |
|-----------|-----------|------|
| `com.fasterxml.jackson.core` | `tools.jackson.core` | 예외: jackson-annotations 유지 |
| `com.fasterxml.jackson.databind` | `tools.jackson.databind` | |
| `com.fasterxml.jackson.datatype.jsr310` | 내장 (별도 의존성 불필요) | |

**주의**: `@JsonProperty`, `@JsonIgnore` 등 어노테이션은 `com.fasterxml.jackson.annotation` 유지

### 2. 클래스/메서드 변경

| Jackson 2 | Jackson 3 | 용도 |
|-----------|-----------|------|
| `ObjectMapper` | `JsonMapper` | JSON 직렬화/역직렬화 |
| `new ObjectMapper()` | `JsonMapper.builder().build()` | 빌더 패턴 필수 |
| `JsonProcessingException` (checked) | `JacksonException` (unchecked) | 예외 처리 |
| `LaissezFaireSubTypeValidator` | `BasicPolymorphicTypeValidator` | 다형성 검증 |
| `ObjectMapper.DefaultTyping` | `DefaultTyping` (독립 enum) | 다형성 타입 설정 |
| `JavaTimeModule` | 내장 | 별도 등록 불필요 |
| `GenericJackson2JsonRedisSerializer` | `GenericJacksonJsonRedisSerializer` | Redis 직렬화 |

**중요**: `DefaultTyping`은 Jackson 3에서 `ObjectMapper` 내부 enum에서 `tools.jackson.databind.DefaultTyping`으로 독립되었습니다.

### 3. API 변경

```java
// Jackson 2: Mutable 설정
ObjectMapper mapper = new ObjectMapper();
mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

// Jackson 3: Immutable Builder 패턴
JsonMapper mapper = JsonMapper.builder()
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    .build();
```

### 4. 기본값 변경

| Feature | Jackson 2 기본값 | Jackson 3 기본값 |
|---------|-----------------|-----------------|
| `WRITE_DATES_AS_TIMESTAMPS` | enabled | **disabled** |
| `FAIL_ON_TRAILING_TOKENS` | disabled | **enabled** |
| `SORT_PROPERTIES_ALPHABETICALLY` | disabled | **enabled** |
| `FAIL_ON_NULL_FOR_PRIMITIVES` | disabled | **enabled** |

### 5. 제거된 기능

- `LaissezFaireSubTypeValidator.instance` → `BasicPolymorphicTypeValidator` 사용
- `ObjectMapper.copy()` → `mapper.rebuild().build()` 사용
- `JavaTimeModule` 별도 등록 → 내장됨 (자동 활성화)
- `MappingJsonFactory` 클래스 제거

---

## 마이그레이션 계획

### Phase 1: JacksonConfig.java 마이그레이션

**현재 코드:**
```java
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Bean
public ObjectMapper objectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    return objectMapper;
}
```

**변경 후:**
```java
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

@Bean
public JsonMapper jsonMapper() {
    return JsonMapper.builder()
        // JavaTimeModule 내장 (별도 등록 불필요)
        // WRITE_DATES_AS_TIMESTAMPS 기본 disabled (설정 불필요)
        .build();
}
```

**변경 포인트:**
- `ObjectMapper` → `JsonMapper`
- `new ObjectMapper()` → `JsonMapper.builder().build()`
- `JavaTimeModule` 등록 제거 (내장)
- `WRITE_DATES_AS_TIMESTAMPS` 설정 제거 (기본 disabled)

---

### Phase 2: CacheConfig.java 마이그레이션 (CRITICAL)

**현재 코드의 문제점:**
1. `LaissezFaireSubTypeValidator` 사용 → Jackson 3에서 제거됨
2. `GenericJackson2JsonRedisSerializer` 사용 → deprecated
3. Mutable ObjectMapper 설정 패턴

**변경 후:**
```java
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;

private RedisCacheConfiguration createDefaultCacheConfiguration() {
    JsonMapper jsonMapper = createCacheJsonMapper();

    // Jackson 3 기반 직렬화기
    GenericJacksonJsonRedisSerializer jsonSerializer =
            new GenericJacksonJsonRedisSerializer(jsonMapper);

    return RedisCacheConfiguration.defaultCacheConfig()
            .serializeKeysWith(...)
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer))
            .disableCachingNullValues()
            .prefixCacheNameWith("delivery::");
}

private JsonMapper createCacheJsonMapper() {
    // 다형성 타입 검증기 (LaissezFaireSubTypeValidator 대체)
    BasicPolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
            .allowIfBaseType(Object.class)  // 또는 더 제한적인 설정
            .allowIfSubType("jjh.delivery.")
            .allowIfSubType("java.util.")
            .allowIfSubType("java.time.")
            .build();

    return JsonMapper.builder()
            // Field visibility 설정
            .changeDefaultVisibility(vc -> vc
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE))

            // 다형성 타입 정보 활성화
            .activateDefaultTypingAsProperty(
                typeValidator,
                JsonMapper.DefaultTyping.NON_FINAL,
                "@class")

            // 역직렬화 설정
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

            // Mixin 등록
            .addMixIn(Product.class, ProductMixin.class)
            .addMixIn(Product.Builder.class, ProductMixin.BuilderMixin.class)
            .addMixIn(Seller.class, SellerMixin.class)
            .addMixIn(Seller.Builder.class, SellerMixin.BuilderMixin.class)
            .addMixIn(Category.class, CategoryMixin.class)
            .addMixIn(Category.Builder.class, CategoryMixin.BuilderMixin.class)
            .build();
}
```

**핵심 변경:**
1. `LaissezFaireSubTypeValidator` → `BasicPolymorphicTypeValidator` (화이트리스트 기반)
2. `GenericJackson2JsonRedisSerializer` → `GenericJacksonJsonRedisSerializer`
3. `ObjectMapper` → `JsonMapper.builder()`
4. `setVisibility()` → `changeDefaultVisibility()`
5. `activateDefaultTyping()` → `activateDefaultTypingAsProperty()`

---

### Phase 3: Mixin 파일 마이그레이션

**변경 없음** - 어노테이션은 `com.fasterxml.jackson.annotation` 패키지 유지

```java
// 그대로 유지
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
```

**주의**: `com.fasterxml.jackson.databind.annotation`은 유지되는 패키지

---

### Phase 4: OrderOutboxAdapter.java 마이그레이션

**현재 코드:**
```java
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

private final ObjectMapper objectMapper;

private String serializeToJson(OrderEvent event) {
    try {
        return objectMapper.writeValueAsString(event);
    } catch (JsonProcessingException e) {  // Checked Exception
        throw new RuntimeException("Failed to serialize", e);
    }
}
```

**변경 후:**
```java
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

private final JsonMapper jsonMapper;

private String serializeToJson(OrderEvent event) {
    // JacksonException은 unchecked - try-catch 불필요
    return jsonMapper.writeValueAsString(event);
}
```

**핵심 변경:**
- `ObjectMapper` → `JsonMapper`
- `JsonProcessingException` (checked) → `JacksonException` (unchecked)
- try-catch 블록 제거 가능

---

### Phase 5: 테스트 파일 마이그레이션

Spring Boot 4.0의 `@WebMvcTest`는 자동으로 Jackson 3 `JsonMapper`를 주입하므로, 테스트 코드에서도 동일하게 변경 필요.

---

## 파일별 변경 상세

### Import 변경 매핑

```java
// ===== 변경되는 Import =====
// Jackson 2                              → Jackson 3
com.fasterxml.jackson.databind.ObjectMapper → tools.jackson.databind.json.JsonMapper
com.fasterxml.jackson.databind.SerializationFeature → tools.jackson.databind.SerializationFeature
com.fasterxml.jackson.databind.DeserializationFeature → tools.jackson.databind.DeserializationFeature
com.fasterxml.jackson.core.JsonProcessingException → tools.jackson.core.JacksonException
com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator → (제거)
com.fasterxml.jackson.datatype.jsr310.JavaTimeModule → (제거, 내장)
GenericJackson2JsonRedisSerializer → GenericJacksonJsonRedisSerializer

// ===== 유지되는 Import (annotations) =====
com.fasterxml.jackson.annotation.JsonAutoDetect (유지)
com.fasterxml.jackson.annotation.JsonTypeInfo (유지)
com.fasterxml.jackson.annotation.PropertyAccessor (유지)
com.fasterxml.jackson.databind.annotation.JsonDeserialize (유지)
com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder (유지)
```

### 작업 순서

```
1. JacksonConfig.java
   └─ ObjectMapper → JsonMapper
   └─ JavaTimeModule 제거
   └─ Feature 설정 빌더 패턴으로 변경

2. CacheConfig.java (가장 복잡)
   └─ Import 변경
   └─ LaissezFaireSubTypeValidator → BasicPolymorphicTypeValidator
   └─ GenericJackson2JsonRedisSerializer → GenericJacksonJsonRedisSerializer
   └─ ObjectMapper 설정 → JsonMapper.builder()
   └─ @SuppressWarnings("removal") 제거

3. OrderOutboxAdapter.java
   └─ ObjectMapper → JsonMapper
   └─ JsonProcessingException → JacksonException
   └─ try-catch 정리

4. Mixin 파일들 (3개)
   └─ Import 확인 (변경 없을 수 있음)

5. OrderControllerTest.java
   └─ ObjectMapper → JsonMapper
```

---

## 검증 체크리스트

### 빌드 검증
- [ ] `./gradlew compileJava` 성공
- [ ] Deprecation 경고 없음
- [ ] Import 충돌 없음

### 기능 검증
- [ ] Redis 캐시 직렬화/역직렬화 정상
- [ ] Product/Seller/Category 캐시 조회 정상
- [ ] OrderOutboxAdapter JSON 직렬화 정상
- [ ] API 응답 JSON 형식 정상

### 호환성 검증
- [ ] java.time 타입 직렬화 (LocalDateTime 등)
- [ ] UUID 직렬화
- [ ] Enum 직렬화
- [ ] Builder 패턴 도메인 객체 역직렬화
- [ ] 다형성 타입 정보 포함 여부

### 성능 검증
- [ ] 직렬화/역직렬화 성능 비교
- [ ] 메모리 사용량 확인

---

## 롤백 계획

문제 발생 시 Spring Boot의 Jackson 2 호환 모드 사용:

```yaml
# application.yaml
spring:
  jackson:
    use-jackson2-defaults: true
```

또는 `spring-boot-jackson2` 의존성으로 교체:

```gradle
// build.gradle
implementation 'org.springframework.boot:spring-boot-jackson2'
```

---

## 예상 작업량

| Phase | 파일 수 | 복잡도 | 예상 변경 라인 |
|-------|--------|--------|---------------|
| Phase 1 | 1 | LOW | ~10 |
| Phase 2 | 1 | HIGH | ~40 |
| Phase 3 | 3 | LOW | ~6 |
| Phase 4 | 1 | MEDIUM | ~15 |
| Phase 5 | 1 | LOW | ~5 |
| **Total** | **7** | - | **~76** |
