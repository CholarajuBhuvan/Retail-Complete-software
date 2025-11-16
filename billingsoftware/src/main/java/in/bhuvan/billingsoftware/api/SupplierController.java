package in.bhuvan.billingsoftware.api;

import in.bhuvan.billingsoftware.domain.Supplier;
import in.bhuvan.billingsoftware.repo.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierRepository supplierRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public List<Supplier> getAllSuppliers(@RequestParam(value = "q", required = false) String query,
                                         @RequestParam(value = "status", required = false) String status) {
        if (query != null && !query.isBlank()) {
            return supplierRepository.searchSuppliers(query);
        }
        if (status != null) {
            return supplierRepository.findByStatus(Supplier.Status.valueOf(status.toUpperCase()));
        }
        return supplierRepository.findAll();
    }

    @GetMapping("/active")
    public List<Supplier> getActiveSuppliers() {
        return supplierRepository.findByStatus(Supplier.Status.ACTIVE);
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public List<Object[]> getSuppliersWithStats() {
        return supplierRepository.findSuppliersWithProductCount();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @SuppressWarnings("null")
    public ResponseEntity<Supplier> getSupplier(@PathVariable Long id) {
        return supplierRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> createSupplier(@RequestBody CreateSupplierRequest request) {
        // Check for duplicate email or phone
        if (request.email() != null && supplierRepository.findByEmail(request.email()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Email already exists"
            ));
        }
        if (request.phone() != null && supplierRepository.findByPhone(request.phone()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Phone already exists"
            ));
        }

        Supplier supplier = new Supplier();
        supplier.setName(request.name());
        supplier.setContactPerson(request.contactPerson());
        supplier.setEmail(request.email());
        supplier.setPhone(request.phone());
        supplier.setAddress(request.address());
        supplier.setWebsite(request.website());
        supplier.setTaxId(request.taxId());
        supplier.setStatus(Supplier.Status.ACTIVE);

        Supplier saved = supplierRepository.save(supplier);
        return ResponseEntity.ok(Map.of("success", true, "supplierId", saved.getId()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @SuppressWarnings("null")
    public ResponseEntity<?> updateSupplier(@PathVariable Long id, @RequestBody UpdateSupplierRequest request) {
        return supplierRepository.findById(id)
                .map(existing -> {
                    // Check for duplicate email or phone (excluding current supplier)
                    if (request.email() != null && !request.email().equals(existing.getEmail())) {
                        if (supplierRepository.findByEmail(request.email()).isPresent()) {
                            return ResponseEntity.badRequest().body(Map.of(
                                    "success", false,
                                    "message", "Email already exists"
                            ));
                        }
                        existing.setEmail(request.email());
                    }
                    if (request.phone() != null && !request.phone().equals(existing.getPhone())) {
                        if (supplierRepository.findByPhone(request.phone()).isPresent()) {
                            return ResponseEntity.badRequest().body(Map.of(
                                    "success", false,
                                    "message", "Phone already exists"
                            ));
                        }
                        existing.setPhone(request.phone());
                    }

                    if (request.name() != null) existing.setName(request.name());
                    if (request.contactPerson() != null) existing.setContactPerson(request.contactPerson());
                    if (request.address() != null) existing.setAddress(request.address());
                    if (request.website() != null) existing.setWebsite(request.website());
                    if (request.taxId() != null) existing.setTaxId(request.taxId());
                    if (request.status() != null) existing.setStatus(request.status());

                    supplierRepository.save(existing);
                    return ResponseEntity.ok(Map.of("success", true));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SuppressWarnings("null")
    public ResponseEntity<?> deleteSupplier(@PathVariable Long id) {
        if (!supplierRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        
        supplierRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    public record CreateSupplierRequest(
            String name,
            String contactPerson,
            String email,
            String phone,
            String address,
            String website,
            String taxId
    ) {}

    public record UpdateSupplierRequest(
            String name,
            String contactPerson,
            String email,
            String phone,
            String address,
            String website,
            String taxId,
            Supplier.Status status
    ) {}
}
