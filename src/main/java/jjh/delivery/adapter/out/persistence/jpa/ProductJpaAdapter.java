package jjh.delivery.adapter.out.persistence.jpa;

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

/**
 * Product JPA Adapter - Driven Adapter (Outbound)
 */
@Component
public class ProductJpaAdapter implements LoadProductPort {

    private final ProductJpaRepository repository;
    private final ProductPersistenceMapper mapper;

    public ProductJpaAdapter(ProductJpaRepository repository, ProductPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> findById(String productId) {
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
    public Page<Product> findBySellerId(String sellerId, ProductStatus status, Pageable pageable) {
        if (status != null) {
            return repository.findBySellerIdAndStatus(sellerId, status, pageable)
                    .map(mapper::toDomain);
        }
        return repository.findBySellerId(sellerId, pageable)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByCategoryId(String categoryId) {
        return repository.countByCategoryIdAndActive(categoryId);
    }

    @Override
    public boolean existsById(String productId) {
        return repository.existsById(productId);
    }

    private Specification<ProductJpaEntity> buildSpecification(SearchProductQuery query) {
        return (root, criteriaQuery, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 상태 필터
            if (query.statuses() != null && !query.statuses().isEmpty()) {
                predicates.add(root.get("status").in(query.statuses()));
            }

            // 판매자 필터
            if (query.sellerId() != null && !query.sellerId().isBlank()) {
                predicates.add(cb.equal(root.get("sellerId"), query.sellerId()));
            }

            // 카테고리 필터
            if (query.categoryId() != null && !query.categoryId().isBlank()) {
                predicates.add(cb.isMember(query.categoryId(), root.get("categoryIds")));
            }

            // 키워드 검색 (이름, 설명)
            if (query.keyword() != null && !query.keyword().isBlank()) {
                String pattern = "%" + query.keyword().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern)
                ));
            }

            // 가격 범위
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
