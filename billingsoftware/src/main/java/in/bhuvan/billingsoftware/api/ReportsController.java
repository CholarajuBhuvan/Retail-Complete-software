package in.bhuvan.billingsoftware.api;

import in.bhuvan.billingsoftware.service.AnalyticsService;
import in.bhuvan.billingsoftware.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportsController {

    private final AnalyticsService analyticsService;
    private final InventoryService inventoryService;

    @GetMapping("/low-stock")
    public Map<String, Object> lowStockReport(@RequestParam(defaultValue = "10") int threshold) {
        return Map.of(
                "threshold", threshold,
                "products", inventoryService.getProductsWithLowStock(threshold),
                "summary", Map.of(
                        "totalLowStock", inventoryService.getProductsWithLowStock(threshold).size(),
                        "outOfStock", inventoryService.getOutOfStockProducts().size()
                )
        );
    }

    @GetMapping("/sales")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Map<String, Object> salesReport(@RequestParam String from, @RequestParam String to) {
        try {
            System.out.println("=== SALES REPORT REQUEST ===");
            System.out.println("From param: " + from);
            System.out.println("To param: " + to);
            
            LocalDateTime fromDate = LocalDateTime.parse(from, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            LocalDateTime toDate = LocalDateTime.parse(to, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            
            System.out.println("Parsed fromDate: " + fromDate);
            System.out.println("Parsed toDate: " + toDate);

            Map<String, Object> result = analyticsService.getSalesAnalytics(fromDate, toDate);
            System.out.println("Sales analytics result: " + result);
            return result;
        } catch (Exception e) {
            System.err.println("=== SALES REPORT ERROR ===");
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();
            return Map.of("error", "Error generating report: " + e.getMessage());
        }
    }

    @GetMapping("/analytics/sales")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Map<String, Object> getAdvancedSalesAnalytics(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        
        LocalDateTime fromDate = from != null ? 
                LocalDateTime.parse(from, DateTimeFormatter.ISO_LOCAL_DATE_TIME) :
                LocalDateTime.now().minusDays(30);
        LocalDateTime toDate = to != null ?
                LocalDateTime.parse(to, DateTimeFormatter.ISO_LOCAL_DATE_TIME) :
                LocalDateTime.now();

        return analyticsService.getSalesAnalytics(fromDate, toDate);
    }

    @GetMapping("/analytics/products")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Map<String, Object> getProductAnalytics() {
        return analyticsService.getProductAnalytics();
    }

    @GetMapping("/analytics/comparison")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Map<String, Object> getPeriodComparison(@RequestParam String period) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime currentStart, currentEnd, previousStart, previousEnd;

        switch (period.toLowerCase()) {
            case "week":
                currentStart = now.minusDays(6).withHour(0).withMinute(0);
                currentEnd = now;
                previousStart = currentStart.minusDays(7);
                previousEnd = currentStart;
                break;
            case "month":
                currentStart = now.minusDays(29).withHour(0).withMinute(0);
                currentEnd = now;
                previousStart = currentStart.minusDays(30);
                previousEnd = currentStart;
                break;
            case "year":
                currentStart = now.minusDays(364).withHour(0).withMinute(0);
                currentEnd = now;
                previousStart = currentStart.minusDays(365);
                previousEnd = currentStart;
                break;
            default:
                return Map.of("error", "Invalid period. Use: week, month, year");
        }

        return analyticsService.getPeriodComparison(currentStart, currentEnd, previousStart, previousEnd);
    }

    @GetMapping("/inventory/alerts")
    public Map<String, Object> getInventoryAlerts() {
        return inventoryService.getInventoryAnalytics();
    }

    @GetMapping("/inventory/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Map<String, Object> getInventoryStats() {
        return inventoryService.getInventoryStats();
    }
    
    @GetMapping("/cashier/{userId}")
    @PreAuthorize("hasRole('MANAGER')")
    public Map<String, Object> getCashierReport(
            @PathVariable Long userId,
            @RequestParam String from, 
            @RequestParam String to) {
        try {
            LocalDateTime fromDate = LocalDateTime.parse(from, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            LocalDateTime toDate = LocalDateTime.parse(to, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            
            return analyticsService.getCashierSalesReport(userId, fromDate, toDate);
        } catch (Exception e) {
            System.err.println("Cashier report error: " + e.getMessage());
            e.printStackTrace();
            return Map.of("error", "Error generating cashier report: " + e.getMessage());
        }
    }
}

