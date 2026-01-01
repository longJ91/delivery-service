package jjh.delivery.adapter.in.web.product;

import jjh.delivery.adapter.in.web.product.dto.CategoryListResponse;
import jjh.delivery.application.port.out.LoadCategoryPort;
import jjh.delivery.domain.category.Category;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Category REST Controller - Driving Adapter (Inbound)
 * 카테고리 조회 API (Public)
 */
@RestController
@RequestMapping("/api/v2/categories")
public class CategoryController {

    private final LoadCategoryPort loadCategoryPort;

    public CategoryController(LoadCategoryPort loadCategoryPort) {
        this.loadCategoryPort = loadCategoryPort;
    }

    /**
     * 카테고리 목록 조회 (트리 구조)
     */
    @GetMapping
    public ResponseEntity<CategoryListResponse> getCategories() {
        List<Category> categories = loadCategoryPort.findAllActiveAsTree();
        return ResponseEntity.ok(CategoryListResponse.from(categories));
    }
}
