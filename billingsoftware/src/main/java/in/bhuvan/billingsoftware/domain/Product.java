package in.bhuvan.billingsoftware.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String sku;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(precision = 12, scale = 2)
    private BigDecimal costPrice; // Purchase price

    @Column(nullable = false)
    private Integer stockQuantity;

    @Column
    private Integer minStockLevel; // Reorder level

    @Column
    private Integer maxStockLevel; // Maximum stock level

    @Column
    private String barcode;

    @Column
    private String unit = "pcs"; // Unit of measurement (pcs, kg, ltr, etc.)

    @Column
    private Boolean trackStock = true; // Whether to track stock for this product

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @Column
    private String imageUrl;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    public enum Status {
        ACTIVE, INACTIVE, DISCONTINUED
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Business logic methods
    public boolean isLowStock() {
        return minStockLevel != null && stockQuantity <= minStockLevel;
    }

    public boolean isOutOfStock() {
        return stockQuantity <= 0;
    }

    public boolean isOverStock() {
        return maxStockLevel != null && stockQuantity >= maxStockLevel;
    }

    public BigDecimal getProfitMargin() {
        if (costPrice == null || costPrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return price.subtract(costPrice).divide(costPrice, 4, java.math.RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
    }
}


