package jjh.delivery.adapter.out.persistence.jpa;

import lombok.RequiredArgsConstructor;

import jjh.delivery.adapter.out.persistence.jpa.entity.CategoryJpaEntity;
import jjh.delivery.adapter.out.persistence.jpa.mapper.CategoryPersistenceMapper;
import jjh.delivery.adapter.out.persistence.jpa.repository.CategoryJpaRepository;
import jjh.delivery.application.port.out.LoadCategoryPort;
import jjh.delivery.config.cache.CacheNames;
import jjh.delivery.domain.category.Category;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Category JPA Adapter - Driven Adapter (Outbound)
 * JPA를 사용한 카테고리 저장/조회 구현
 */
@Component
@RequiredArgsConstructor
public class CategoryJpaAdapter implements LoadCategoryPort {

    private final CategoryJpaRepository repository;
    private final CategoryPersistenceMapper mapper;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheNames.CATEGORIES, keyGenerator = CacheNames.ENTITY_KEY_GENERATOR)
    public Optional<Category> findById(UUID categoryId) {
        return repository.findById(categoryId)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheNames.CATEGORIES, key = "'tree'")
    public List<Category> findAllActiveAsTree() {
        var entities = repository.findAllActiveOrderByDepthAndDisplayOrder();
        return buildTree(entities);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> findByParentId(UUID parentId) {
        return repository.findByParentIdAndIsActiveTrueOrderByDisplayOrder(parentId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheNames.CATEGORIES, key = "'roots'")
    public List<Category> findRootCategories() {
        return repository.findRootCategoriesActive().stream()
                .map(mapper::toDomain)
                .toList();
    }

    /**
     * JPA Entities → Domain Category Tree 구성
     */
    private List<Category> buildTree(List<CategoryJpaEntity> entities) {
        Map<UUID, Category> categoryMap = entities.stream()
                .collect(Collectors.toMap(
                        CategoryJpaEntity::getId,
                        mapper::toDomain,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        List<Category> roots = new ArrayList<>();

        for (CategoryJpaEntity entity : entities) {
            Category category = categoryMap.get(entity.getId());
            if (entity.getParentId() == null) {
                roots.add(category);
            } else {
                Category parent = categoryMap.get(entity.getParentId());
                if (parent != null) {
                    parent.addChild(category);
                }
            }
        }

        return roots;
    }
}
