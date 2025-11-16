package in.bhuvan.billingsoftware.repo;

import in.bhuvan.billingsoftware.domain.Payment;
import in.bhuvan.billingsoftware.domain.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    List<Payment> findByOrder(CustomerOrder order);
    
    List<Payment> findByMethod(Payment.PaymentMethod method);
    
    List<Payment> findByStatus(Payment.PaymentStatus status);
    
    @Query("SELECT p FROM Payment p WHERE p.createdAt >= :startDate AND p.createdAt < :endDate")
    List<Payment> findByDateRange(@Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate);
    
    @Query(value = "SELECT method, COUNT(*), SUM(amount) FROM payments WHERE status = 'COMPLETED' AND created_at >= ?1 GROUP BY method", nativeQuery = true)
    List<Object[]> getPaymentMethodStats(OffsetDateTime startDate);
    
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'COMPLETED' AND p.method = :method AND p.createdAt >= :startDate")
    BigDecimal getTotalAmountByMethod(@Param("method") Payment.PaymentMethod method, @Param("startDate") OffsetDateTime startDate);
}
