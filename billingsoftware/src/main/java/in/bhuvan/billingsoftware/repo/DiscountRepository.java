package in.bhuvan.billingsoftware.repo;

import in.bhuvan.billingsoftware.domain.Discount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DiscountRepository extends JpaRepository<Discount, Long> {
    
    Optional<Discount> findByCode(String code);
    
    List<Discount> findByStatus(Discount.Status status);
    
    @Query("SELECT d FROM Discount d WHERE d.status = 'ACTIVE' AND (d.validFrom IS NULL OR d.validFrom <= :now) AND (d.validTo IS NULL OR d.validTo >= :now)")
    List<Discount> findActiveDiscounts(@Param("now") LocalDateTime now);
    
    @Query("SELECT d FROM Discount d WHERE LOWER(d.code) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(d.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Discount> searchDiscounts(@Param("query") String query);
    
    @Query("SELECT d FROM Discount d WHERE d.status = 'ACTIVE' AND d.validTo < :now")
    List<Discount> findExpiredDiscounts(@Param("now") LocalDateTime now);
    
    @Query("SELECT d FROM Discount d JOIN d.applicableProducts p WHERE p.id = :productId AND d.status = 'ACTIVE'")
    List<Discount> findByProductId(@Param("productId") Long productId);
    
    @Query("SELECT d FROM Discount d JOIN d.applicableCategories c WHERE c.id = :categoryId AND d.status = 'ACTIVE'")
    List<Discount> findByCategoryId(@Param("categoryId") Long categoryId);
    
    boolean existsByCode(String code);
}
