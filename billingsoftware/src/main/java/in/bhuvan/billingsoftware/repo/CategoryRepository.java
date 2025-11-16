package in.bhuvan.billingsoftware.repo;

import in.bhuvan.billingsoftware.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    Optional<Category> findByName(String name);
    
    boolean existsByName(String name);
    
    @Query("SELECT c FROM Category c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(c.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Category> search(@Param("query") String query);
    
    @Query("SELECT c FROM Category c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(c.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Category> searchCategories(@Param("query") String query);
    
    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.products ORDER BY c.name")
    List<Category> findAllWithProducts();
    
    @Query("SELECT c, COUNT(p) as productCount FROM Category c LEFT JOIN c.products p GROUP BY c ORDER BY c.name")
    List<Object[]> findCategoriesWithProductCount();
}
