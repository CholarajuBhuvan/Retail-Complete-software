package in.bhuvan.billingsoftware.service;

import in.bhuvan.billingsoftware.domain.CustomerOrder;
import in.bhuvan.billingsoftware.repo.CustomerOrderRepository;
import in.bhuvan.billingsoftware.repo.ProductRepository;
import in.bhuvan.billingsoftware.repo.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final CustomerOrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final PaymentRepository paymentRepository;

    public Map<String, Object> getSalesAnalytics(LocalDateTime from, LocalDateTime to) {
        OffsetDateTime fromOffset = from.atZone(ZoneId.systemDefault()).toOffsetDateTime();
        OffsetDateTime toOffset = to.atZone(ZoneId.systemDefault()).toOffsetDateTime();
        
        System.out.println("=== SALES ANALYTICS DEBUG ===");
        System.out.println("From LocalDateTime: " + from);
        System.out.println("To LocalDateTime: " + to);
        System.out.println("From OffsetDateTime: " + fromOffset);
        System.out.println("To OffsetDateTime: " + toOffset);
        
        Long totalOrders = orderRepository.countOrdersByDateRange(fromOffset, toOffset);
        BigDecimal totalRevenue = orderRepository.sumTotalByDateRange(fromOffset, toOffset);
        
        System.out.println("Total Orders Found: " + totalOrders);
        System.out.println("Total Revenue: " + totalRevenue);
        
        List<Object[]> dailySales = orderRepository.getDailySalesReport(fromOffset);
        List<Object[]> paymentMethodStats = paymentRepository.getPaymentMethodStats(fromOffset);
        System.out.println("=== END DEBUG ===");

        BigDecimal avgOrderValue = BigDecimal.ZERO;
        if (totalOrders > 0) {
            avgOrderValue = totalRevenue.divide(new BigDecimal(totalOrders), 2, RoundingMode.HALF_UP);
        }

        return Map.of(
                "totalOrders", totalOrders != null ? totalOrders : 0L,
                "totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO,
                "averageOrderValue", avgOrderValue,
                "dailySales", dailySales,
                "paymentMethodBreakdown", paymentMethodStats,
                "period", Map.of("from", from, "to", to)
        );
    }

    public Map<String, Object> getProductAnalytics() {
        Long totalProducts = productRepository.countActiveProducts();
        Long lowStockProducts = productRepository.countLowStockProducts();
        Long outOfStockProducts = productRepository.countOutOfStockProducts();
        BigDecimal totalInventoryValue = productRepository.calculateTotalInventoryValue();

        return Map.of(
                "totalActiveProducts", totalProducts != null ? totalProducts : 0L,
                "lowStockProducts", lowStockProducts != null ? lowStockProducts : 0L,
                "outOfStockProducts", outOfStockProducts != null ? outOfStockProducts : 0L,
                "totalInventoryValue", totalInventoryValue != null ? totalInventoryValue : BigDecimal.ZERO,
                "stockHealthPercentage", calculateStockHealthPercentage(totalProducts, lowStockProducts, outOfStockProducts)
        );
    }

    public Map<String, Object> getPeriodComparison(LocalDateTime currentStart, LocalDateTime currentEnd,
                                                   LocalDateTime previousStart, LocalDateTime previousEnd) {
        Map<String, Object> currentPeriod = getSalesAnalytics(currentStart, currentEnd);
        Map<String, Object> previousPeriod = getSalesAnalytics(previousStart, previousEnd);

        BigDecimal currentRevenue = (BigDecimal) currentPeriod.get("totalRevenue");
        BigDecimal previousRevenue = (BigDecimal) previousPeriod.get("totalRevenue");
        Long currentOrders = (Long) currentPeriod.get("totalOrders");
        Long previousOrders = (Long) previousPeriod.get("totalOrders");

        BigDecimal revenueGrowth = calculateGrowthPercentage(previousRevenue, currentRevenue);
        BigDecimal orderGrowth = calculateGrowthPercentage(
            new BigDecimal(previousOrders), new BigDecimal(currentOrders)
        );

        return Map.of(
                "current", currentPeriod,
                "previous", previousPeriod,
                "growth", Map.of(
                        "revenue", revenueGrowth,
                        "orders", orderGrowth
                )
        );
    }

    public Map<String, Object> getTopSellingProducts(LocalDateTime from, LocalDateTime to, int limit) {
        // This would require order items analysis - simplified for now
        return Map.of(
                "period", Map.of("from", from, "to", to),
                "topProducts", List.of(), // Would implement with actual order items data
                "message", "Top selling products analysis - requires order items tracking"
        );
    }
    
    public Map<String, Object> getCashierSalesReport(Long userId, LocalDateTime from, LocalDateTime to) {
        OffsetDateTime fromOffset = from.atZone(ZoneId.systemDefault()).toOffsetDateTime();
        OffsetDateTime toOffset = to.atZone(ZoneId.systemDefault()).toOffsetDateTime();
        
        Long totalOrders = orderRepository.countOrdersByUser(userId, fromOffset, toOffset);
        BigDecimal totalRevenue = orderRepository.sumTotalByUser(userId, fromOffset, toOffset);
        List<CustomerOrder> orders = orderRepository.findOrdersByUser(userId, fromOffset, toOffset);
        
        BigDecimal avgOrderValue = BigDecimal.ZERO;
        if (totalOrders > 0) {
            avgOrderValue = totalRevenue.divide(new BigDecimal(totalOrders), 2, RoundingMode.HALF_UP);
        }
        
        return Map.of(
                "userId", userId,
                "totalOrders", totalOrders != null ? totalOrders : 0L,
                "totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO,
                "averageOrderValue", avgOrderValue,
                "orders", orders,
                "period", Map.of("from", from, "to", to)
        );
    }

    private BigDecimal calculateStockHealthPercentage(Long total, Long lowStock, Long outOfStock) {
        if (total == null || total == 0) {
            return BigDecimal.ZERO;
        }
        
        long problematicStock = (outOfStock != null ? outOfStock : 0) + (lowStock != null ? lowStock : 0);
        long healthyStock = total - problematicStock;
        
        return BigDecimal.valueOf(healthyStock)
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    private BigDecimal calculateGrowthPercentage(BigDecimal previous, BigDecimal current) {
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) > 0 ? new BigDecimal("100") : BigDecimal.ZERO;
        }
        
        return current.subtract(previous)
                .divide(previous, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }
}
