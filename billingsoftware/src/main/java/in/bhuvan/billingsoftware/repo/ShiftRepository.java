package in.bhuvan.billingsoftware.repo;

import in.bhuvan.billingsoftware.domain.Shift;
import in.bhuvan.billingsoftware.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, Long> {
    
    List<Shift> findByEmployee(User employee);
    
    List<Shift> findByStatus(Shift.ShiftStatus status);
    
    @Query("SELECT s FROM Shift s WHERE s.employee.id = :employeeId AND s.status = 'ACTIVE'")
    Optional<Shift> findActiveShiftByEmployee(@Param("employeeId") Long employeeId);
    
    @Query("SELECT s FROM Shift s WHERE s.clockIn >= :startDate AND s.clockIn < :endDate")
    List<Shift> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT s FROM Shift s WHERE s.employee.id = :employeeId AND s.clockIn >= :startDate AND s.clockIn < :endDate")
    List<Shift> findByEmployeeAndDateRange(@Param("employeeId") Long employeeId, 
                                          @Param("startDate") LocalDateTime startDate, 
                                          @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(s) FROM Shift s WHERE s.status = 'ACTIVE'")
    Long countActiveShifts();
    
    @Query("SELECT SUM(s.salesAmount) FROM Shift s WHERE s.clockIn >= :startDate AND s.status = 'COMPLETED'")
    BigDecimal getTotalSalesByDateRange(@Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT s.employee, COUNT(s), SUM(s.salesAmount) FROM Shift s WHERE s.clockIn >= :startDate GROUP BY s.employee ORDER BY SUM(s.salesAmount) DESC")
    List<Object[]> getEmployeePerformanceStats(@Param("startDate") LocalDateTime startDate);
}
