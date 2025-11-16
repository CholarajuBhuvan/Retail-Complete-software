package in.bhuvan.billingsoftware.api;

import in.bhuvan.billingsoftware.domain.Product;
import in.bhuvan.billingsoftware.repo.ProductRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@SuppressWarnings("null")
public class ProductController {

    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER', 'EMPLOYEE')")
    public List<Product> list(@RequestParam(value = "q", required = false) String query) {
        if (query == null || query.isBlank()) {
            return productRepository.findAll();
        }
        // Search by name, SKU, or barcode
        return productRepository.search(query);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER', 'EMPLOYEE')")
    public ResponseEntity<Product> get(@PathVariable Long id) {
        return productRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/sku/{sku}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    public ResponseEntity<Product> getBySku(@PathVariable String sku) {
        return productRepository.findBySku(sku)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Product> create(@RequestBody Product product) {
        if (product.getId() != null) return ResponseEntity.badRequest().build();
        if (product.getSku() == null || product.getSku().isBlank()) return ResponseEntity.badRequest().build();
        if (productRepository.existsBySku(product.getSku())) return ResponseEntity.status(409).build();
        
        if (product.getCreatedAt() == null) product.setCreatedAt(LocalDateTime.now());
        if (product.getUpdatedAt() == null) product.setUpdatedAt(LocalDateTime.now());
        if (product.getStatus() == null) product.setStatus(Product.Status.ACTIVE);
        if (product.getTrackStock() == null) product.setTrackStock(true);
        
        Product saved = productRepository.save(product);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Product> update(@PathVariable Long id, @RequestBody Product changes) {
        return productRepository.findById(id)
                .map(existing -> {
                    if (changes.getName() != null) existing.setName(changes.getName());
                    if (changes.getSku() != null) existing.setSku(changes.getSku());
                    if (changes.getPrice() != null) existing.setPrice(changes.getPrice());
                    if (changes.getCostPrice() != null) existing.setCostPrice(changes.getCostPrice());
                    if (changes.getStockQuantity() != null) existing.setStockQuantity(changes.getStockQuantity());
                    if (changes.getBarcode() != null) existing.setBarcode(changes.getBarcode());
                    if (changes.getUnit() != null) existing.setUnit(changes.getUnit());
                    if (changes.getMinStockLevel() != null) existing.setMinStockLevel(changes.getMinStockLevel());
                    if (changes.getMaxStockLevel() != null) existing.setMaxStockLevel(changes.getMaxStockLevel());
                    if (changes.getDescription() != null) existing.setDescription(changes.getDescription());
                    if (changes.getStatus() != null) existing.setStatus(changes.getStatus());
                    existing.setUpdatedAt(LocalDateTime.now());
                    return ResponseEntity.ok(productRepository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SuppressWarnings("null")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        if (!productRepository.existsById(id)) return ResponseEntity.notFound().build();
        productRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}


