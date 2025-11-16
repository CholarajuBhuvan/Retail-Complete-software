package in.bhuvan.billingsoftware.bootstrap;

import in.bhuvan.billingsoftware.domain.Customer;
import in.bhuvan.billingsoftware.repo.CustomerRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomerSeeder {

    @Bean
    CommandLineRunner seedCustomers(CustomerRepository customers) {
        return args -> {
            if (customers.count() == 0) {
                Customer demo = new Customer();
                demo.setName("Walk-in Customer");
                demo.setPhone("0000000000");
                demo.setEmail("walkin@example.com");
                customers.save(demo);
            }
        };
    }
}


