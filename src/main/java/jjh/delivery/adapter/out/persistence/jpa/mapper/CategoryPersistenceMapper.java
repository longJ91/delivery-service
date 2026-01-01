package jjh.delivery.adapter.out.persistence.jpa.mapper;

import jjh.delivery.adapter.out.persistence.jpa.entity.CategoryJpaEntity;
import jjh.delivery.domain.category.Category;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Category 영속성 매퍼
 */
@Component
public class CategoryPersistenceMapper {

    public Category toDomain(CategoryJpaEntity entity) {
        return Category.builder()
                .id(entity.getId())
                .parentId(entity.getParentId())
                .name(entity.getName())
                .description(entity.getDescription())
                .imageUrl(entity.getImageUrl())
                .displayOrder(entity.getDisplayOrder())
                .depth(entity.getDepth())
                .isActive(entity.isActive())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public CategoryJpaEntity toEntity(Category domain) {
        return new CategoryJpaEntity(
                domain.getId(),
                domain.getParentId(),
                domain.getName(),
                domain.getDescription(),
                domain.getImageUrl(),
                domain.getDisplayOrder(),
                domain.getDepth(),
                domain.isActive(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }

    /**
     * 플랫 리스트를 트리 구조로 변환
     */
    public List<Category> toTree(List<CategoryJpaEntity> entities) {
        // 먼저 모든 엔티티를 도메인 객체로 변환
        List<Category> allCategories = entities.stream()
                .map(this::toDomain)
                .toList();

        // ID -> Category 맵 생성
        Map<String, Category> categoryMap = allCategories.stream()
                .collect(Collectors.toMap(Category::getId, c -> c));

        // 트리 구축
        List<Category> roots = new ArrayList<>();
        for (Category category : allCategories) {
            if (category.getParentId() == null) {
                roots.add(category);
            } else {
                Category parent = categoryMap.get(category.getParentId());
                if (parent != null) {
                    parent.addChild(category);
                }
            }
        }

        return roots;
    }
}
