package in.bhuvan.billingsoftware.repo;

import in.bhuvan.billingsoftware.domain.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    
    Optional<Supplier> findByName(String name);
    
    List<Supplier> findByStatus(Supplier.Status status);
    
    Optional<Supplier> findByEmail(String email);
    
    Optional<Supplier> findByPhone(String phone);
    
    @Query("SELECT s FROM Supplier s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(s.contactPerson) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(s.email) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Supplier> search(@Param("query") String query);
    
    @Query("SELECT s FROM Supplier s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(s.contactPerson) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(s.email) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Supplier> searchSuppliers(@Param("query") String query);
    
    @Query("SELECT s FROM Supplier s LEFT JOIN FETCH s.products WHERE s.status = :status ORDER BY s.name")
    List<Supplier> findByStatusWithProducts(@Param("status") Supplier.Status status);
    
    @Query("SELECT s, COUNT(p) as productCount FROM Supplier s LEFT JOIN s.products p GROUP BY s ORDER BY s.name")
    List<Object[]> findSuppliersWithProductCount();
}
