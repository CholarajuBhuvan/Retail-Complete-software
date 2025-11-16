package in.bhuvan.billingsoftware.api;

import in.bhuvan.billingsoftware.repo.CustomerOrderRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/export")
public class ExportController {

    private final CustomerOrderRepository orders;

    public ExportController(CustomerOrderRepository orders) {
        this.orders = orders;
    }

    @GetMapping(value = "/sales.csv", produces = "text/csv")
    public ResponseEntity<byte[]> exportSales(@RequestParam String from, @RequestParam String to) {
        OffsetDateTime fromTs = OffsetDateTime.parse(from);
        OffsetDateTime toTs = OffsetDateTime.parse(to);
        var list = orders.findAll().stream()
                .filter(o -> !o.getCreatedAt().isBefore(fromTs) && !o.getCreatedAt().isAfter(toTs))
                .toList();
        StringBuilder sb = new StringBuilder();
        sb.append("orderId,customer,createdAt,subtotal,tax,total\n");
        list.forEach(o -> sb.append(o.getId()).append(',')
                .append('"').append(o.getCustomer().getName()).append('"').append(',')
                .append(o.getCreatedAt()).append(',')
                .append(o.getSubtotal()).append(',')
                .append(o.getTax()).append(',')
                .append(o.getTotal()).append('\n'));
        byte[] data = sb.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=sales.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(data);
    }
}


