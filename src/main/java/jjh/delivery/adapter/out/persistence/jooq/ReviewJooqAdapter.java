package jjh.delivery.adapter.out.persistence.jooq;

import jjh.delivery.adapter.out.persistence.jooq.repository.ReviewJooqRepository;
import jjh.delivery.application.port.out.LoadReviewStatsPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Review jOOQ Adapter - Driven Adapter (Outbound)
 * jOOQ를 사용한 리뷰 통계 조회 구현
 * 컴파일 타임 타입 안전성 확보
 */
@Repository
@RequiredArgsConstructor
public class ReviewJooqAdapter implements LoadReviewStatsPort {

    private final ReviewJooqRepository repository;

    @Override
    @Transactional(readOnly = true)
    public double getAverageRatingByProductId(String productId) {
        return repository.getAverageRatingByProductId(productId);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Integer, Long> getRatingDistributionByProductId(String productId) {
        Map<Integer, Long> resultMap = repository.getRatingDistributionByProductId(productId);

        // 1~5 모든 평점에 대해 기본값 0 보장
        return IntStream.rangeClosed(1, 5)
                .boxed()
                .collect(Collectors.toMap(
                        Function.identity(),
                        rating -> resultMap.getOrDefault(rating, 0L)
                ));
    }
}
