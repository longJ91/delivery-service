package jjh.delivery.config.cache.mixin;

import jjh.delivery.domain.product.Product;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * Jackson 3 Mixin for Product domain object
 *
 * 도메인 객체의 순수성을 유지하면서 Jackson 역직렬화 지원
 * Builder 패턴을 사용하는 도메인 객체의 캐시 저장/조회를 위한 설정
 */
@JsonDeserialize(builder = Product.Builder.class)
public abstract class ProductMixin {

    @JsonPOJOBuilder(withPrefix = "")
    public static abstract class BuilderMixin {
    }
}
