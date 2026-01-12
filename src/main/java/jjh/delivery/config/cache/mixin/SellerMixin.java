package jjh.delivery.config.cache.mixin;

import jjh.delivery.domain.seller.Seller;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * Jackson 3 Mixin for Seller domain object
 *
 * 도메인 객체의 순수성을 유지하면서 Jackson 역직렬화 지원
 * Builder 패턴을 사용하는 도메인 객체의 캐시 저장/조회를 위한 설정
 */
@JsonDeserialize(builder = Seller.Builder.class)
public abstract class SellerMixin {

    @JsonPOJOBuilder(withPrefix = "")
    public static abstract class BuilderMixin {
    }
}
