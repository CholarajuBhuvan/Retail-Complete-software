package in.bhuvan.billingsoftware.api;

import in.bhuvan.billingsoftware.domain.Product;
import in.bhuvan.billingsoftware.repo.ProductRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/inventory")
@SuppressWarnings("null")
public class InventoryController {

    private final ProductRepository products;

    public InventoryController(ProductRepository products) {
        this.products = products;
    }

    @PostMapping("/receive")
    public ResponseEntity<Product> receive(@RequestBody ReceiveStockRequest req) {
        Product p = products.findById(req.productId()).orElse(null);
        if (p == null) return ResponseEntity.notFound().build();
        int add = Math.max(0, req.quantity());
        p.setStockQuantity(p.getStockQuantity() + add);
        if (req.newUnitPrice() != null && req.newUnitPrice().compareTo(BigDecimal.ZERO) > 0) {
            p.setPrice(req.newUnitPrice());
        }
        return ResponseEntity.ok(products.save(p));
    }

    public record ReceiveStockRequest(Long productId, int quantity, BigDecimal newUnitPrice) {}
}


