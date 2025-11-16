package in.bhuvan.billingsoftware.api;

import in.bhuvan.billingsoftware.service.InventoryService;
import in.bhuvan.billingsoftware.repo.CustomerOrderRepository;
import in.bhuvan.billingsoftware.repo.CustomerRepository;
import in.bhuvan.billingsoftware.repo.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final InventoryService inventoryService;
    private final CustomerOrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    @GetMapping("/overview")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    public Map<String, Object> getDashboardOverview() {
        Map<String, Object> inventoryStats = inventoryService.getInventoryStats();
        
        // Sales stats
        Long allOrders = orderRepository.count();
        BigDecimal allRevenue = orderRepository.findAll().stream()
                .map(order -> order.getTotal() != null ? order.getTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Customer stats
        Long totalCustomers = customerRepository.count();
        Long activeProducts = productRepository.countActiveProducts();
        Long totalProducts = productRepository.count();
        
        Object lowStockCount = inventoryStats.get("lowStockCount");
        
        return Map.of(
                "totalSales", allRevenue != null ? allRevenue : BigDecimal.ZERO,
                "totalOrders", allOrders != null ? allOrders : 0L,
                "totalProducts", totalProducts != null ? totalProducts : 0L,
                "activeProducts", activeProducts != null ? activeProducts : 0L,
                "totalCustomers", totalCustomers != null ? totalCustomers : 0L,
                "lowStockCount", lowStockCount != null ? lowStockCount : 0
        );
    }

    @GetMapping("/inventory-alerts")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE', 'CASHIER')")
    public Object getInventoryAlerts() {
        Map<String, Object> analytics = inventoryService.getInventoryAnalytics();
        return analytics.get("lowStockProducts");
    }

    @GetMapping("/quick-stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    public Map<String, Object> getQuickStats() {
        // Today's stats
        OffsetDateTime todayStart = LocalDate.now().atStartOfDay().atZone(ZoneId.systemDefault()).toOffsetDateTime();
        OffsetDateTime todayEnd = todayStart.plusDays(1);
        Long todayOrders = orderRepository.countOrdersByDateRange(todayStart, todayEnd);
        BigDecimal todayRevenue = orderRepository.sumTotalByDateRange(todayStart, todayEnd);
        
        // This week stats
        OffsetDateTime weekStart = LocalDate.now().minusDays(6).atStartOfDay().atZone(ZoneId.systemDefault()).toOffsetDateTime();
        OffsetDateTime now = OffsetDateTime.now();
        Long weekOrders = orderRepository.countOrdersByDateRange(weekStart, now);
        BigDecimal weekRevenue = orderRepository.sumTotalByDateRange(weekStart, now);
        
        // This month stats
        OffsetDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay().atZone(ZoneId.systemDefault()).toOffsetDateTime();
        Long monthOrders = orderRepository.countOrdersByDateRange(monthStart, now);
        BigDecimal monthRevenue = orderRepository.sumTotalByDateRange(monthStart, now);
        
        return Map.of(
                "todaySales", todayRevenue != null ? todayRevenue : BigDecimal.ZERO,
                "todayOrders", todayOrders != null ? todayOrders : 0L,
                "weekSales", weekRevenue != null ? weekRevenue : BigDecimal.ZERO,
                "weekOrders", weekOrders != null ? weekOrders : 0L,
                "monthSales", monthRevenue != null ? monthRevenue : BigDecimal.ZERO,
                "monthOrders", monthOrders != null ? monthOrders : 0L
        );
    }
}
