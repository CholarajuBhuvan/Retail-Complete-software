package in.bhuvan.billingsoftware.api;

import in.bhuvan.billingsoftware.repo.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/barcodes")
@RequiredArgsConstructor
@SuppressWarnings("null")
public class BarcodeController {

    private final ProductRepository productRepository;

    @GetMapping("/scan/{barcode}")
    public ResponseEntity<?> scanBarcode(@PathVariable String barcode) {
        return productRepository.findByBarcode(barcode)
                .map(product -> ResponseEntity.ok(Map.of(
                        "success", true,
                        "product", product
                )))
                .orElse(ResponseEntity.ok(Map.of(
                        "success", false,
                        "message", "Product not found for barcode: " + barcode
                )));
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generateBarcode(@RequestBody GenerateBarcodeRequest request) {
        if (productRepository.existsByBarcode(request.barcode())) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Barcode already exists"
            ));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "barcode", request.barcode(),
                "type", "CODE128" // Standard barcode format
        ));
    }

    @PostMapping("/auto-generate")
    public ResponseEntity<?> autoGenerateBarcode() {
        String barcode;
        do {
            // Generate EAN-13 compatible barcode (13 digits)
            barcode = "200" + System.currentTimeMillis() % 10000000000L;
            barcode = String.format("%013d", Long.parseLong(barcode));
        } while (productRepository.existsByBarcode(barcode));

        return ResponseEntity.ok(Map.of(
                "success", true,
                "barcode", barcode,
                "type", "EAN13"
        ));
    }

    @PutMapping("/assign/{productId}")
    public ResponseEntity<?> assignBarcodeToProduct(@PathVariable Long productId, @RequestBody AssignBarcodeRequest request) {
        return productRepository.findById(productId)
                .map(product -> {
                    if (request.barcode() != null && productRepository.existsByBarcode(request.barcode())) {
                        return ResponseEntity.badRequest().body(Map.of(
                                "success", false,
                                "message", "Barcode already exists"
                        ));
                    }

                    product.setBarcode(request.barcode());
                    productRepository.save(product);
                    
                    return ResponseEntity.ok(Map.of(
                            "success", true,
                            "message", "Barcode assigned successfully"
                    ));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    public record GenerateBarcodeRequest(String barcode) {}
    public record AssignBarcodeRequest(String barcode) {}
}
