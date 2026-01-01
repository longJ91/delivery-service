package jjh.delivery.adapter.out.persistence.jpa.repository;

import jjh.delivery.adapter.out.persistence.jpa.entity.CategoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Category JPA Repository
 */
public interface CategoryJpaRepository extends JpaRepository<CategoryJpaEntity, String> {

    /**
     * 활성 카테고리 전체 조회 (정렬: depth, displayOrder)
     */
    @Query("SELECT c FROM CategoryJpaEntity c WHERE c.isActive = true ORDER BY c.depth, c.displayOrder")
    List<CategoryJpaEntity> findAllActiveOrderByDepthAndDisplayOrder();

    /**
     * 부모 ID로 자식 카테고리 조회
     */
    List<CategoryJpaEntity> findByParentIdAndIsActiveTrueOrderByDisplayOrder(String parentId);

    /**
     * 최상위 카테고리 조회 (parentId = null)
     */
    @Query("SELECT c FROM CategoryJpaEntity c WHERE c.parentId IS NULL AND c.isActive = true ORDER BY c.displayOrder")
    List<CategoryJpaEntity> findRootCategoriesActive();
}
