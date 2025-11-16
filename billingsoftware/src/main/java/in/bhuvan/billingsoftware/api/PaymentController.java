package in.bhuvan.billingsoftware.api;

import in.bhuvan.billingsoftware.domain.Payment;
import in.bhuvan.billingsoftware.repo.PaymentRepository;
import in.bhuvan.billingsoftware.repo.CustomerOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@SuppressWarnings("null")
public class PaymentController {

    private final PaymentRepository paymentRepository;
    private final CustomerOrderRepository orderRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public List<Payment> getAllPayments(@RequestParam(value = "method", required = false) String method,
                                       @RequestParam(value = "status", required = false) String status) {
        if (method != null) {
            return paymentRepository.findByMethod(Payment.PaymentMethod.valueOf(method.toUpperCase()));
        }
        if (status != null) {
            return paymentRepository.findByStatus(Payment.PaymentStatus.valueOf(status.toUpperCase()));
        }
        return paymentRepository.findAll();
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<Payment>> getPaymentsByOrder(@PathVariable Long orderId) {
        return orderRepository.findById(orderId)
                .map(order -> ResponseEntity.ok(paymentRepository.findByOrder(order)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Map<String, Object> getPaymentStats(@RequestParam(value = "days", defaultValue = "30") int days) {
        OffsetDateTime startDate = LocalDateTime.now().minusDays(days).atZone(ZoneId.systemDefault()).toOffsetDateTime();
        List<Object[]> methodStats = paymentRepository.getPaymentMethodStats(startDate);
        
        Map<String, Object> stats = Map.of(
                "methodStats", methodStats,
                "totalCash", paymentRepository.getTotalAmountByMethod(Payment.PaymentMethod.CASH, startDate),
                "totalCard", paymentRepository.getTotalAmountByMethod(Payment.PaymentMethod.CARD, startDate),
                "totalUPI", paymentRepository.getTotalAmountByMethod(Payment.PaymentMethod.UPI, startDate)
        );
        
        return stats;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    public ResponseEntity<?> createPayment(@RequestBody CreatePaymentRequest request) {
        return orderRepository.findById(request.orderId())
                .map(order -> {
                    Payment payment = new Payment();
                    payment.setOrder(order);
                    payment.setMethod(request.method());
                    payment.setAmount(request.amount());
                    payment.setReferenceNumber(request.referenceNumber());
                    payment.setCardLast4(request.cardLast4());
                    payment.setStatus(Payment.PaymentStatus.COMPLETED);
                    payment.setNotes(request.notes());

                    Payment saved = paymentRepository.save(payment);
                    return ResponseEntity.ok(Map.of("success", true, "paymentId", saved.getId()));
                })
                .orElse(ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Order not found"
                )));
    }

    @PutMapping("/{id}/refund")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> refundPayment(@PathVariable Long id, @RequestBody RefundRequest request) {
        return paymentRepository.findById(id)
                .map(payment -> {
                    if (payment.getStatus() != Payment.PaymentStatus.COMPLETED) {
                        return ResponseEntity.badRequest().body(Map.of(
                                "success", false,
                                "message", "Payment cannot be refunded"
                        ));
                    }

                    payment.setStatus(request.partial() ? Payment.PaymentStatus.PARTIALLY_REFUNDED : Payment.PaymentStatus.REFUNDED);
                    payment.setNotes((payment.getNotes() != null ? payment.getNotes() + "; " : "") + "Refund: " + request.reason());
                    
                    paymentRepository.save(payment);
                    return ResponseEntity.ok(Map.of("success", true));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    public record CreatePaymentRequest(
            Long orderId,
            Payment.PaymentMethod method,
            BigDecimal amount,
            String referenceNumber,
            String cardLast4,
            String notes
    ) {}

    public record RefundRequest(boolean partial, String reason) {}
}
