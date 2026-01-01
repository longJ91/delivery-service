package jjh.delivery.domain.category.exception;

/**
 * 카테고리를 찾을 수 없을 때 발생하는 예외
 */
public class CategoryNotFoundException extends RuntimeException {

    private final String categoryId;

    public CategoryNotFoundException(String categoryId) {
        super("Category not found: " + categoryId);
        this.categoryId = categoryId;
    }

    public String getCategoryId() {
        return categoryId;
    }
}
