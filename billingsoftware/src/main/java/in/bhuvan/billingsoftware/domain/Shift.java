package in.bhuvan.billingsoftware.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Entity
@Table(name = "shifts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Shift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User employee;

    @Column(nullable = false)
    private LocalDateTime clockIn;

    @Column
    private LocalDateTime clockOut;

    @Column(precision = 12, scale = 2)
    private BigDecimal salesAmount = BigDecimal.ZERO;

    @Column
    private Integer ordersProcessed = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShiftStatus status = ShiftStatus.ACTIVE;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column
    private String location; // Store location or register number

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    public enum ShiftStatus {
        ACTIVE, COMPLETED, CANCELLED
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Business logic methods
    public long getShiftDurationMinutes() {
        if (clockOut == null) {
            return java.time.Duration.between(clockIn, LocalDateTime.now()).toMinutes();
        }
        return java.time.Duration.between(clockIn, clockOut).toMinutes();
    }

    public BigDecimal getHourlyRate() {
        long minutes = getShiftDurationMinutes();
        if (minutes == 0) return BigDecimal.ZERO;
        
        BigDecimal hours = new BigDecimal(minutes).divide(new BigDecimal("60"), 2, RoundingMode.HALF_UP);
        return salesAmount.divide(hours, 2, RoundingMode.HALF_UP);
    }

    public boolean isActive() {
        return status == ShiftStatus.ACTIVE && clockOut == null;
    }
}
