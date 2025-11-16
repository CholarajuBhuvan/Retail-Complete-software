package in.bhuvan.billingsoftware.api;

import in.bhuvan.billingsoftware.repo.CustomerOrderRepository;
import in.bhuvan.billingsoftware.service.ReceiptService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/receipts")
@SuppressWarnings("null")
public class ReceiptController {

    private final CustomerOrderRepository orders;
    private final ReceiptService receipts;

    public ReceiptController(CustomerOrderRepository orders, ReceiptService receipts) {
        this.orders = orders;
        this.receipts = receipts;
    }

    @GetMapping("/{orderId}.pdf")
    public ResponseEntity<byte[]> receipt(@PathVariable Long orderId) {
        var order = orders.findById(orderId).orElse(null);
        if (order == null) return ResponseEntity.notFound().build();
        byte[] pdf = receipts.generateReceipt(order);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=receipt-" + orderId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}


