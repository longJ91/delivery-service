package jjh.delivery.adapter.out.persistence.jpa;

import lombok.RequiredArgsConstructor;

import jakarta.persistence.criteria.Predicate;
import jjh.delivery.adapter.in.web.dto.CursorPageResponse;
import jjh.delivery.adapter.in.web.dto.CursorValue;
import jjh.delivery.adapter.out.persistence.jpa.entity.ProductJpaEntity;
import jjh.delivery.adapter.out.persistence.jpa.mapper.ProductPersistenceMapper;
import jjh.delivery.adapter.out.persistence.jpa.repository.ProductJpaRepository;
import jjh.delivery.application.port.out.LoadProductPort;
import jjh.delivery.domain.product.Product;
import jjh.delivery.domain.product.ProductStatus;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Product JPA Adapter - Driven Adapter (Outbound)
 * JPA를 사용한 상품 조회 구현 (커서 기반 페이지네이션)
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
    public CursorPageResponse<Product> searchProducts(SearchProductQuery query) {
        Specification<ProductJpaEntity> spec = buildSpecification(query);

        // 커서 조건 추가
        CursorValue cursorValue = CursorValue.decode(query.cursor());
        if (cursorValue != null) {
            spec = spec.and(buildCursorCondition(cursorValue));
        }

        // size + 1 조회하여 hasNext 판단
        List<ProductJpaEntity> entities = repository.findAll(spec,
                org.springframework.data.domain.PageRequest.of(0, query.size() + 1,
                        org.springframework.data.domain.Sort.by(
                                org.springframework.data.domain.Sort.Order.desc("createdAt"),
                                org.springframework.data.domain.Sort.Order.desc("id")
                        ))).getContent();

        List<Product> products = entities.stream()
                .map(mapper::toDomain)
                .toList();

        return CursorPageResponse.ofWithUuidId(
                products,
                query.size(),
                product -> product.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant(),
                Product::getId
        );
    }

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponse<Product> findBySellerId(UUID sellerId, ProductStatus status, String cursor, int size) {
        CursorValue cursorValue = CursorValue.decode(cursor);

        List<ProductJpaEntity> entities;
        if (cursorValue != null) {
            LocalDateTime cursorCreatedAt = LocalDateTime.ofInstant(cursorValue.createdAt(), ZoneId.systemDefault());
            if (status != null) {
                entities = repository.findBySellerIdAndStatusWithCursor(
                        sellerId, status, cursorCreatedAt, cursorValue.id(), size + 1);
            } else {
                entities = repository.findBySellerIdWithCursor(
                        sellerId, cursorCreatedAt, cursorValue.id(), size + 1);
            }
        } else {
            if (status != null) {
                entities = repository.findBySellerIdAndStatusOrderByCreatedAtDesc(sellerId, status, size + 1);
            } else {
                entities = repository.findBySellerIdOrderByCreatedAtDesc(sellerId, size + 1);
            }
        }

        List<Product> products = entities.stream()
                .map(mapper::toDomain)
                .toList();

        return CursorPageResponse.ofWithUuidId(
                products,
                size,
                product -> product.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant(),
                Product::getId
        );
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

    /**
     * 커서 조건을 위한 Specification 생성
     * Keyset pagination: (created_at, id) DESC 순서
     */
    private Specification<ProductJpaEntity> buildCursorCondition(CursorValue cursorValue) {
        return (root, criteriaQuery, cb) -> {
            LocalDateTime cursorCreatedAt = LocalDateTime.ofInstant(cursorValue.createdAt(), ZoneId.systemDefault());
            return cb.or(
                    cb.lessThan(root.get("createdAt"), cursorCreatedAt),
                    cb.and(
                            cb.equal(root.get("createdAt"), cursorCreatedAt),
                            cb.lessThan(root.get("id"), cursorValue.id())
                    )
            );
        };
    }
}
