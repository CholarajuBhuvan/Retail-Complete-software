package in.bhuvan.billingsoftware.api;

import in.bhuvan.billingsoftware.domain.Category;
import in.bhuvan.billingsoftware.repo.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@SuppressWarnings("null")
public class CategoryController {

    private final CategoryRepository categoryRepository;

    @GetMapping
    public List<Category> getAllCategories(@RequestParam(value = "q", required = false) String query) {
        if (query != null && !query.isBlank()) {
            return categoryRepository.searchCategories(query);
        }
        return categoryRepository.findAll();
    }

    @GetMapping("/with-products")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public List<Category> getCategoriesWithProducts() {
        return categoryRepository.findAllWithProducts();
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public List<Object[]> getCategoriesWithStats() {
        return categoryRepository.findCategoriesWithProductCount();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        return categoryRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> createCategory(@RequestBody CreateCategoryRequest request) {
        if (categoryRepository.existsByName(request.name())) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Category name already exists"
            ));
        }

        Category category = new Category();
        category.setName(request.name());
        category.setDescription(request.description());
        category.setColor(request.color());

        Category saved = categoryRepository.save(category);
        return ResponseEntity.ok(Map.of("success", true, "categoryId", saved.getId()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @SuppressWarnings("null")
    public ResponseEntity<?> updateCategory(@PathVariable Long id, @RequestBody UpdateCategoryRequest request) {
        return categoryRepository.findById(id)
                .map(existing -> {
                    if (request.name() != null && !request.name().equals(existing.getName())) {
                        if (categoryRepository.existsByName(request.name())) {
                            return ResponseEntity.badRequest().body(Map.of(
                                    "success", false,
                                    "message", "Category name already exists"
                            ));
                        }
                        existing.setName(request.name());
                    }
                    if (request.description() != null) existing.setDescription(request.description());
                    if (request.color() != null) existing.setColor(request.color());

                    categoryRepository.save(existing);
                    return ResponseEntity.ok(Map.of("success", true));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SuppressWarnings("null")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        if (!categoryRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        
        categoryRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    public record CreateCategoryRequest(String name, String description, String color) {}
    public record UpdateCategoryRequest(String name, String description, String color) {}
}
