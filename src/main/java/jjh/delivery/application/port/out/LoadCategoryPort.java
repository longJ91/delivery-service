package jjh.delivery.application.port.out;

import jjh.delivery.domain.category.Category;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Category 조회 Port - Driven Port (Outbound)
 */
public interface LoadCategoryPort {

    /**
     * ID로 카테고리 조회
     */
    Optional<Category> findById(UUID categoryId);

    /**
     * 모든 활성 카테고리 조회 (트리 구조)
     */
    List<Category> findAllActiveAsTree();

    /**
     * 부모 카테고리의 자식 카테고리 조회
     */
    List<Category> findByParentId(UUID parentId);

    /**
     * 최상위 카테고리 조회
     */
    List<Category> findRootCategories();
}
