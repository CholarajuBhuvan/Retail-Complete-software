package in.bhuvan.billingsoftware.bootstrap;

import in.bhuvan.billingsoftware.domain.Product;
import in.bhuvan.billingsoftware.repo.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
@SuppressWarnings("null")
public class DataSeeder {

    @Bean
    CommandLineRunner seedInitialData(ProductRepository productRepository) {
        return args -> {
            if (productRepository.count() == 0) {
                productRepository.save(product("SKU-APPLE", "Apple", new BigDecimal("0.50"), 1000));
                productRepository.save(product("SKU-BANANA", "Banana", new BigDecimal("0.30"), 1000));
                productRepository.save(product("SKU-MILK", "Milk 1L", new BigDecimal("1.20"), 500));
                productRepository.save(product("SKU-BREAD", "Bread", new BigDecimal("1.00"), 300));
                productRepository.save(product("SKU-EGGS", "Eggs (12)", new BigDecimal("2.50"), 200));
            }
        };
    }

    private static Product product(String sku, String name, BigDecimal price, int stock) {
        Product p = new Product();
        p.setSku(sku);
        p.setName(name);
        p.setPrice(price);
        p.setStockQuantity(stock);
        return p;
    }
}


