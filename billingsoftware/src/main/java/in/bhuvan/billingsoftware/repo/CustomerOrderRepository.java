package in.bhuvan.billingsoftware.repo;

import in.bhuvan.billingsoftware.domain.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {
    
    @Query("SELECT COUNT(o) FROM CustomerOrder o WHERE o.createdAt >= :startDate AND o.createdAt <= :endDate")
    Long countOrdersByDateRange(@Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate);
    
    @Query("SELECT COALESCE(SUM(o.total), 0) FROM CustomerOrder o WHERE o.createdAt >= :startDate AND o.createdAt <= :endDate")
    BigDecimal sumTotalByDateRange(@Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate);
    
    @Query("SELECT o FROM CustomerOrder o WHERE o.createdAt >= :startDate AND o.createdAt <= :endDate ORDER BY o.createdAt DESC")
    List<CustomerOrder> findOrdersByDateRange(@Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate);
    
    @Query("SELECT o FROM CustomerOrder o WHERE o.customer.id = :customerId ORDER BY o.createdAt DESC")
    List<CustomerOrder> findByCustomerId(@Param("customerId") Long customerId);
    
    @Query(value = "SELECT CAST(created_at AS DATE) as orderDate, COUNT(*) as orderCount, SUM(total) as dailyTotal FROM orders WHERE created_at >= ?1 GROUP BY CAST(created_at AS DATE) ORDER BY CAST(created_at AS DATE)", nativeQuery = true)
    List<Object[]> getDailySalesReport(OffsetDateTime startDate);
    
    // Cashier-specific reports
    @Query("SELECT COUNT(o) FROM CustomerOrder o WHERE o.processedBy.id = :userId AND o.createdAt >= :startDate AND o.createdAt <= :endDate")
    Long countOrdersByUser(@Param("userId") Long userId, @Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate);
    
    @Query("SELECT COALESCE(SUM(o.total), 0) FROM CustomerOrder o WHERE o.processedBy.id = :userId AND o.createdAt >= :startDate AND o.createdAt <= :endDate")
    BigDecimal sumTotalByUser(@Param("userId") Long userId, @Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate);
    
    @Query("SELECT o FROM CustomerOrder o WHERE o.processedBy.id = :userId AND o.createdAt >= :startDate AND o.createdAt <= :endDate ORDER BY o.createdAt DESC")
    List<CustomerOrder> findOrdersByUser(@Param("userId") Long userId, @Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate);
}


