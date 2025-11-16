package in.bhuvan.billingsoftware.api;

import in.bhuvan.billingsoftware.domain.Discount;
import in.bhuvan.billingsoftware.repo.DiscountRepository;
import in.bhuvan.billingsoftware.repo.ProductRepository;
import in.bhuvan.billingsoftware.repo.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/discounts")
@RequiredArgsConstructor
@SuppressWarnings("null")
public class DiscountController {

    private final DiscountRepository discountRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public List<Discount> getAllDiscounts(@RequestParam(value = "q", required = false) String query,
                                         @RequestParam(value = "status", required = false) String status) {
        if (query != null && !query.isBlank()) {
            return discountRepository.searchDiscounts(query);
        }
        if (status != null) {
            return discountRepository.findByStatus(Discount.Status.valueOf(status.toUpperCase()));
        }
        return discountRepository.findAll();
    }

    @GetMapping("/active")
    public List<Discount> getActiveDiscounts() {
        return discountRepository.findActiveDiscounts(LocalDateTime.now());
    }

    @GetMapping("/validate/{code}")
    public ResponseEntity<?> validateDiscount(@PathVariable String code,
                                            @RequestParam BigDecimal orderAmount) {
        return discountRepository.findByCode(code)
                .map(discount -> {
                    if (!discount.isValid()) {
                        return ResponseEntity.ok(Map.of(
                                "valid", false,
                                "message", "Discount is not valid or has expired"
                        ));
                    }

                    BigDecimal discountAmount = discount.calculateDiscount(orderAmount);
                    return ResponseEntity.ok(Map.of(
                            "valid", true,
                            "discount", discount,
                            "discountAmount", discountAmount,
                            "finalAmount", orderAmount.subtract(discountAmount)
                    ));
                })
                .orElse(ResponseEntity.ok(Map.of(
                        "valid", false,
                        "message", "Discount code not found"
                )));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @SuppressWarnings("null")
    public ResponseEntity<?> createDiscount(@RequestBody CreateDiscountRequest request) {
        if (discountRepository.existsByCode(request.code())) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Discount code already exists"
            ));
        }

        Discount discount = new Discount();
        discount.setCode(request.code());
        discount.setName(request.name());
        discount.setDescription(request.description());
        discount.setType(request.type());
        discount.setValue(request.value());
        discount.setMinOrderAmount(request.minOrderAmount());
        discount.setMaxDiscountAmount(request.maxDiscountAmount());
        discount.setUsageLimit(request.usageLimit());
        discount.setUsageLimitPerCustomer(request.usageLimitPerCustomer());
        discount.setValidFrom(request.validFrom());
        discount.setValidTo(request.validTo());
        discount.setStatus(Discount.Status.ACTIVE);

        // Set applicable products
        if (request.productIds() != null && !request.productIds().isEmpty()) {
            Set<in.bhuvan.billingsoftware.domain.Product> products = Set.copyOf(
                productRepository.findAllById(request.productIds())
            );
            discount.setApplicableProducts(products);
        }

        // Set applicable categories
        if (request.categoryIds() != null && !request.categoryIds().isEmpty()) {
            Set<in.bhuvan.billingsoftware.domain.Category> categories = Set.copyOf(
                categoryRepository.findAllById(request.categoryIds())
            );
            discount.setApplicableCategories(categories);
        }

        Discount saved = discountRepository.save(discount);
        return ResponseEntity.ok(Map.of("success", true, "discountId", saved.getId()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> updateDiscount(@PathVariable Long id, @RequestBody UpdateDiscountRequest request) {
        return discountRepository.findById(id)
                .map(existing -> {
                    if (request.code() != null && !request.code().equals(existing.getCode())) {
                        if (discountRepository.existsByCode(request.code())) {
                            return ResponseEntity.badRequest().body(Map.of(
                                    "success", false,
                                    "message", "Discount code already exists"
                            ));
                        }
                        existing.setCode(request.code());
                    }

                    if (request.name() != null) existing.setName(request.name());
                    if (request.description() != null) existing.setDescription(request.description());
                    if (request.type() != null) existing.setType(request.type());
                    if (request.value() != null) existing.setValue(request.value());
                    if (request.minOrderAmount() != null) existing.setMinOrderAmount(request.minOrderAmount());
                    if (request.maxDiscountAmount() != null) existing.setMaxDiscountAmount(request.maxDiscountAmount());
                    if (request.usageLimit() != null) existing.setUsageLimit(request.usageLimit());
                    if (request.usageLimitPerCustomer() != null) existing.setUsageLimitPerCustomer(request.usageLimitPerCustomer());
                    if (request.validFrom() != null) existing.setValidFrom(request.validFrom());
                    if (request.validTo() != null) existing.setValidTo(request.validTo());
                    if (request.status() != null) existing.setStatus(request.status());

                    discountRepository.save(existing);
                    return ResponseEntity.ok(Map.of("success", true));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SuppressWarnings("null")
    public ResponseEntity<?> deleteDiscount(@PathVariable Long id) {
        if (!discountRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        
        discountRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    public record CreateDiscountRequest(
            String code,
            String name,
            String description,
            Discount.DiscountType type,
            BigDecimal value,
            BigDecimal minOrderAmount,
            BigDecimal maxDiscountAmount,
            Integer usageLimit,
            Integer usageLimitPerCustomer,
            LocalDateTime validFrom,
            LocalDateTime validTo,
            List<Long> productIds,
            List<Long> categoryIds
    ) {}

    public record UpdateDiscountRequest(
            String code,
            String name,
            String description,
            Discount.DiscountType type,
            BigDecimal value,
            BigDecimal minOrderAmount,
            BigDecimal maxDiscountAmount,
            Integer usageLimit,
            Integer usageLimitPerCustomer,
            LocalDateTime validFrom,
            LocalDateTime validTo,
            Discount.Status status
    ) {}
}
