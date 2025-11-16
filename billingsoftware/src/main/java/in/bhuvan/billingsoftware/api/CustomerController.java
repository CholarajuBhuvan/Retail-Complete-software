package in.bhuvan.billingsoftware.api;

import in.bhuvan.billingsoftware.domain.Customer;
import in.bhuvan.billingsoftware.repo.CustomerRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerRepository customers;

    public CustomerController(CustomerRepository customers) {
        this.customers = customers;
    }

    @GetMapping
    public List<Customer> list(@RequestParam(value = "q", required = false) String q) {
        if (q == null || q.isBlank()) return customers.findAll();
        // simple contains search on name or phone in-memory for demo
        return customers.findAll().stream()
                .filter(c -> (c.getName() != null && c.getName().toLowerCase().contains(q.toLowerCase()))
                        || (c.getPhone() != null && c.getPhone().contains(q)))
                .toList();
    }

    @PostMapping
    public ResponseEntity<Customer> create(@RequestBody Customer c) {
        if (c.getId() != null) return ResponseEntity.badRequest().build();
        if (c.getName() == null || c.getName().isBlank()) return ResponseEntity.badRequest().build();
        if (c.getPhone() == null || c.getPhone().isBlank()) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(customers.save(c));
    }
}


