package in.bhuvan.billingsoftware.service;

import in.bhuvan.billingsoftware.domain.Product;
import in.bhuvan.billingsoftware.repo.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryService {

    private final ProductRepository productRepository;

    @SuppressWarnings("null")
    public Product adjustStock(Long productId, Integer quantity, String reason) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        
        if (!product.getTrackStock()) {
            throw new IllegalArgumentException("Stock tracking is disabled for this product");
        }

        int newQuantity = Math.max(0, product.getStockQuantity() + quantity);
        product.setStockQuantity(newQuantity);
        
        return productRepository.save(product);
    }

    @SuppressWarnings("null")
    public Product receiveStock(Long productId, Integer quantity, BigDecimal newCostPrice) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        
        if (!product.getTrackStock()) {
            throw new IllegalArgumentException("Stock tracking is disabled for this product");
        }

        product.setStockQuantity(product.getStockQuantity() + quantity);
        
        if (newCostPrice != null && newCostPrice.compareTo(BigDecimal.ZERO) > 0) {
            product.setCostPrice(newCostPrice);
        }
        
        return productRepository.save(product);
    }

    public List<Product> getLowStockProducts() {
        return productRepository.findLowStockProducts();
    }

    public List<Product> getOutOfStockProducts() {
        return productRepository.findOutOfStockProducts();
    }

    public List<Product> getOverStockProducts() {
        return productRepository.findOverStockProducts();
    }

    public List<Product> getProductsWithLowStock(Integer threshold) {
        return productRepository.findProductsWithLowStock(threshold);
    }

    public Map<String, Object> getInventoryStats() {
        Long totalProducts = productRepository.countActiveProducts();
        Long outOfStockCount = productRepository.countOutOfStockProducts();
        Long lowStockCount = productRepository.countLowStockProducts();
        BigDecimal totalValue = productRepository.calculateTotalInventoryValue();

        return Map.of(
                "totalProducts", totalProducts != null ? totalProducts : 0L,
                "outOfStockCount", outOfStockCount != null ? outOfStockCount : 0L,
                "lowStockCount", lowStockCount != null ? lowStockCount : 0L,
                "totalInventoryValue", totalValue != null ? totalValue : BigDecimal.ZERO,
                "stockHealthPercentage", calculateStockHealthPercentage(totalProducts, outOfStockCount, lowStockCount)
        );
    }

    private BigDecimal calculateStockHealthPercentage(Long total, Long outOfStock, Long lowStock) {
        if (total == null || total == 0) {
            return BigDecimal.ZERO;
        }
        
        long problematicStock = (outOfStock != null ? outOfStock : 0) + (lowStock != null ? lowStock : 0);
        long healthyStock = total - problematicStock;
        
        return BigDecimal.valueOf(healthyStock)
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    public List<Product> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return productRepository.findByPriceRange(minPrice, maxPrice);
    }

    @SuppressWarnings("null")
    public Product updateReorderLevels(Long productId, Integer minLevel, Integer maxLevel) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        
        if (minLevel != null) {
            product.setMinStockLevel(minLevel);
        }
        if (maxLevel != null) {
            product.setMaxStockLevel(maxLevel);
        }
        
        return productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getInventoryAnalytics() {
        List<Product> lowStockProducts = getLowStockProducts();
        List<Product> outOfStockProducts = getOutOfStockProducts();
        List<Product> overStockProducts = getOverStockProducts();
        
        return Map.of(
                "lowStockProducts", lowStockProducts,
                "outOfStockProducts", outOfStockProducts,
                "overStockProducts", overStockProducts,
                "alerts", Map.of(
                        "lowStock", lowStockProducts.size(),
                        "outOfStock", outOfStockProducts.size(),
                        "overStock", overStockProducts.size()
                )
        );
    }
}
