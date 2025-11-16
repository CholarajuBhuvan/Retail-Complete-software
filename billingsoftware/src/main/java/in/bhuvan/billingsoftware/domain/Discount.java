package in.bhuvan.billingsoftware.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "discounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Discount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountType type;

    @Column(name = "\"value\"", nullable = false, precision = 12, scale = 2)
    private BigDecimal value; // Percentage or fixed amount

    @Column(precision = 12, scale = 2)
    private BigDecimal minOrderAmount; // Minimum order amount to apply discount

    @Column(precision = 12, scale = 2)
    private BigDecimal maxDiscountAmount; // Maximum discount amount (for percentage discounts)

    @Column
    private Integer usageLimit; // Total usage limit

    @Column
    private Integer usageCount = 0; // Current usage count

    @Column
    private Integer usageLimitPerCustomer; // Usage limit per customer

    @Column
    private LocalDateTime validFrom;

    @Column
    private LocalDateTime validTo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE;

    @ManyToMany
    @JoinTable(
        name = "discount_products",
        joinColumns = @JoinColumn(name = "discount_id"),
        inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private Set<Product> applicableProducts = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "discount_categories",
        joinColumns = @JoinColumn(name = "discount_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> applicableCategories = new HashSet<>();

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    public enum DiscountType {
        PERCENTAGE, FIXED_AMOUNT, BUY_X_GET_Y
    }

    public enum Status {
        ACTIVE, INACTIVE, EXPIRED
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
    public boolean isValid() {
        if (status != Status.ACTIVE) return false;
        
        LocalDateTime now = LocalDateTime.now();
        if (validFrom != null && now.isBefore(validFrom)) return false;
        if (validTo != null && now.isAfter(validTo)) return false;
        
        if (usageLimit != null && usageCount >= usageLimit) return false;
        
        return true;
    }

    public boolean isApplicableToProduct(Product product) {
        if (applicableProducts.isEmpty() && applicableCategories.isEmpty()) {
            return true; // Applies to all products
        }
        
        if (applicableProducts.contains(product)) {
            return true;
        }
        
        return applicableCategories.contains(product.getCategory());
    }

    public BigDecimal calculateDiscount(BigDecimal orderAmount) {
        if (!isValid()) return BigDecimal.ZERO;
        
        if (minOrderAmount != null && orderAmount.compareTo(minOrderAmount) < 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal discount = BigDecimal.ZERO;
        
        switch (type) {
            case PERCENTAGE:
                discount = orderAmount.multiply(value).divide(new BigDecimal("100"));
                if (maxDiscountAmount != null && discount.compareTo(maxDiscountAmount) > 0) {
                    discount = maxDiscountAmount;
                }
                break;
            case FIXED_AMOUNT:
                discount = value;
                if (discount.compareTo(orderAmount) > 0) {
                    discount = orderAmount;
                }
                break;
            case BUY_X_GET_Y:
                // This would require more complex logic based on order items
                break;
        }
        
        return discount;
    }
}
