package in.bhuvan.billingsoftware.repo;

import in.bhuvan.billingsoftware.domain.Category;
import in.bhuvan.billingsoftware.domain.Product;
import in.bhuvan.billingsoftware.domain.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByNameContainingIgnoreCase(String name);

    Optional<Product> findBySku(String sku);
    
    Optional<Product> findByBarcode(String barcode);

    boolean existsBySku(String sku);
    
    boolean existsByBarcode(String barcode);

    List<Product> findByCategory(Category category);
    
    List<Product> findBySupplier(Supplier supplier);
    
    List<Product> findByStatus(Product.Status status);

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.barcode) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Product> search(@Param("query") String query);

    @Query("SELECT p FROM Product p WHERE p.stockQuantity <= p.minStockLevel AND p.minStockLevel IS NOT NULL AND p.trackStock = true")
    List<Product> findLowStockProducts();

    @Query("SELECT p FROM Product p WHERE p.stockQuantity <= 0 AND p.trackStock = true")
    List<Product> findOutOfStockProducts();

    @Query("SELECT p FROM Product p WHERE p.stockQuantity >= p.maxStockLevel AND p.maxStockLevel IS NOT NULL AND p.trackStock = true")
    List<Product> findOverStockProducts();

    @Query("SELECT p FROM Product p WHERE p.stockQuantity <= :threshold AND p.trackStock = true")
    List<Product> findProductsWithLowStock(@Param("threshold") Integer threshold);

    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice")
    List<Product> findByPriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.supplier WHERE p.status = :status")
    List<Product> findByStatusWithDetails(@Param("status") Product.Status status);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.status = 'ACTIVE'")
    Long countActiveProducts();

    @Query("SELECT COUNT(p) FROM Product p WHERE p.stockQuantity <= 0 AND p.trackStock = true")
    Long countOutOfStockProducts();

    @Query("SELECT COUNT(p) FROM Product p WHERE p.stockQuantity <= p.minStockLevel AND p.minStockLevel IS NOT NULL AND p.trackStock = true")
    Long countLowStockProducts();

    @Query("SELECT SUM(p.stockQuantity * p.price) FROM Product p WHERE p.status = 'ACTIVE'")
    BigDecimal calculateTotalInventoryValue();
}
