package jjh.delivery.adapter.out.persistence.jpa;

import lombok.RequiredArgsConstructor;

import jakarta.persistence.criteria.Predicate;
import jjh.delivery.adapter.out.persistence.jpa.entity.ProductJpaEntity;
import jjh.delivery.adapter.out.persistence.jpa.mapper.ProductPersistenceMapper;
import jjh.delivery.adapter.out.persistence.jpa.repository.ProductJpaRepository;
import jjh.delivery.application.port.out.LoadProductPort;
import jjh.delivery.domain.product.Product;
import jjh.delivery.domain.product.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Product JPA Adapter - Driven Adapter (Outbound)
 * JPA를 사용한 상품 조회 구현
 * Note: 통계 쿼리(countByCategoryId)는 ProductJooqAdapter로 분리됨
 */
@Component
@RequiredArgsConstructor
public class ProductJpaAdapter implements LoadProductPort {

    private final ProductJpaRepository repository;
    private final ProductPersistenceMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> findById(UUID productId) {
        return repository.findByIdWithVariants(productId)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> searchProducts(SearchProductQuery query, Pageable pageable) {
        Specification<ProductJpaEntity> spec = buildSpecification(query);
        return repository.findAll(spec, pageable)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> findBySellerId(UUID sellerId, ProductStatus status, Pageable pageable) {
        if (status != null) {
            return repository.findBySellerIdAndStatus(sellerId, status, pageable)
                    .map(mapper::toDomain);
        }
        return repository.findBySellerId(sellerId, pageable)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsById(UUID productId) {
        return repository.existsById(productId);
    }

    private Specification<ProductJpaEntity> buildSpecification(SearchProductQuery query) {
        return (root, criteriaQuery, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (query.statuses() != null && !query.statuses().isEmpty()) {
                predicates.add(root.get("status").in(query.statuses()));
            }

            if (query.sellerId() != null) {
                predicates.add(cb.equal(root.get("sellerId"), query.sellerId()));
            }

            if (query.categoryId() != null) {
                predicates.add(cb.isMember(query.categoryId(), root.get("categoryIds")));
            }

            if (query.keyword() != null && !query.keyword().isBlank()) {
                String pattern = "%" + query.keyword().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern)
                ));
            }

            if (query.minPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("basePrice"), query.minPrice()));
            }
            if (query.maxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("basePrice"), query.maxPrice()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
