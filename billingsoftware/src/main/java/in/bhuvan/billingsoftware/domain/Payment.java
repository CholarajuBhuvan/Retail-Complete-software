package in.bhuvan.billingsoftware.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private CustomerOrder order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column
    private String referenceNumber; // For card/digital payments

    @Column
    private String cardLast4; // Last 4 digits of card

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status = PaymentStatus.COMPLETED;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime processedAt;

    public enum PaymentMethod {
        CASH, CARD, UPI, NET_BANKING, WALLET, GIFT_CARD, CREDIT
    }

    public enum PaymentStatus {
        PENDING, COMPLETED, FAILED, REFUNDED, PARTIALLY_REFUNDED
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == PaymentStatus.COMPLETED) {
            processedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        if (status == PaymentStatus.COMPLETED && processedAt == null) {
            processedAt = LocalDateTime.now();
        }
    }
}
