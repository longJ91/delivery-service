package jjh.delivery.adapter.out.persistence.jooq;

import jjh.delivery.adapter.out.persistence.jooq.repository.SellerJooqRepository;
import jjh.delivery.application.port.out.LoadSellerInfoPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Seller jOOQ Adapter - Driven Adapter (Outbound)
 * jOOQ를 사용한 판매자 정보 조회 구현
 * 컴파일 타임 타입 안전성 확보
 */
@Repository
@RequiredArgsConstructor
public class SellerJooqAdapter implements LoadSellerInfoPort {

    private final SellerJooqRepository repository;

    @Override
    @Transactional(readOnly = true)
    public Optional<String> findBusinessNameById(String sellerId) {
        return repository.findBusinessNameById(sellerId);
    }
}
