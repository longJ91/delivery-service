package jjh.delivery.adapter.out.persistence.jooq;

import jjh.delivery.adapter.out.persistence.jooq.repository.ProductJooqRepository;
import jjh.delivery.application.port.out.LoadProductStatsPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Product jOOQ Adapter - Driven Adapter (Outbound)
 * jOOQ를 사용한 상품 통계 조회 구현
 * 컴파일 타임 타입 안전성 확보
 */
@Repository
@RequiredArgsConstructor
public class ProductJooqAdapter implements LoadProductStatsPort {

    private final ProductJooqRepository repository;

    @Override
    @Transactional(readOnly = true)
    public long countByCategoryId(String categoryId) {
        return repository.countByCategoryIdAndActive(categoryId);
    }
}
