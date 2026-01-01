package jjh.delivery.adapter.out.persistence.jpa;

import jjh.delivery.adapter.out.persistence.jpa.mapper.CategoryPersistenceMapper;
import jjh.delivery.adapter.out.persistence.jpa.repository.CategoryJpaRepository;
import jjh.delivery.application.port.out.LoadCategoryPort;
import jjh.delivery.domain.category.Category;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Category JPA Adapter - Driven Adapter (Outbound)
 */
@Component
public class CategoryJpaAdapter implements LoadCategoryPort {

    private final CategoryJpaRepository repository;
    private final CategoryPersistenceMapper mapper;

    public CategoryJpaAdapter(CategoryJpaRepository repository, CategoryPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Category> findById(String categoryId) {
        return repository.findById(categoryId)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> findAllActiveAsTree() {
        var entities = repository.findAllActiveOrderByDepthAndDisplayOrder();
        return mapper.toTree(entities);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> findByParentId(String parentId) {
        return repository.findByParentIdAndIsActiveTrueOrderByDisplayOrder(parentId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> findRootCategories() {
        return repository.findRootCategoriesActive().stream()
                .map(mapper::toDomain)
                .toList();
    }
}
