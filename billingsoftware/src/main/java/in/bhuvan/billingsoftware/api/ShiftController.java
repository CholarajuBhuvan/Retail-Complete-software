package in.bhuvan.billingsoftware.api;

import in.bhuvan.billingsoftware.domain.Shift;
import in.bhuvan.billingsoftware.domain.User;
import in.bhuvan.billingsoftware.repo.ShiftRepository;
import in.bhuvan.billingsoftware.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/shifts")
@RequiredArgsConstructor
@SuppressWarnings("null")
public class ShiftController {

    private final ShiftRepository shiftRepository;
    private final UserService userService;

    @PostMapping("/clock-in")
    public ResponseEntity<?> clockIn(@RequestBody ClockInRequest request, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        
        // Check if user already has an active shift
        if (shiftRepository.findActiveShiftByEmployee(user.getId()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "You already have an active shift"
            ));
        }

        Shift shift = new Shift();
        shift.setEmployee(user);
        shift.setClockIn(LocalDateTime.now());
        shift.setLocation(request.location());
        shift.setNotes(request.notes());
        shift.setStatus(Shift.ShiftStatus.ACTIVE);

        Shift saved = shiftRepository.save(shift);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "shiftId", saved.getId(),
                "clockInTime", saved.getClockIn()
        ));
    }

    @PostMapping("/clock-out/{shiftId}")
    public ResponseEntity<?> clockOut(@PathVariable Long shiftId, @RequestBody ClockOutRequest request, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        
        return shiftRepository.findById(shiftId)
                .filter(shift -> shift.getEmployee().getId().equals(user.getId()) || 
                               user.getRole() == User.Role.ADMIN || user.getRole() == User.Role.MANAGER)
                .map(shift -> {
                    if (!shift.isActive()) {
                        return ResponseEntity.badRequest().body(Map.of(
                                "success", false,
                                "message", "Shift is not active"
                        ));
                    }

                    shift.setClockOut(LocalDateTime.now());
                    shift.setStatus(Shift.ShiftStatus.COMPLETED);
                    if (request.salesAmount() != null) {
                        shift.setSalesAmount(request.salesAmount());
                    }
                    if (request.ordersProcessed() != null) {
                        shift.setOrdersProcessed(request.ordersProcessed());
                    }
                    if (request.notes() != null) {
                        shift.setNotes((shift.getNotes() != null ? shift.getNotes() + "; " : "") + request.notes());
                    }

                    shiftRepository.save(shift);
                    return ResponseEntity.ok(Map.of(
                            "success", true,
                            "shiftDuration", shift.getShiftDurationMinutes(),
                            "salesAmount", shift.getSalesAmount()
                    ));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/current")
    public ResponseEntity<?> getCurrentShift(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        
        return shiftRepository.findActiveShiftByEmployee(user.getId())
                .map(shift -> ResponseEntity.ok(Map.of(
                        "hasActiveShift", true,
                        "shift", shift,
                        "currentDuration", shift.getShiftDurationMinutes()
                )))
                .orElse(ResponseEntity.ok(Map.of("hasActiveShift", false)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public List<Shift> getAllShifts(@RequestParam(value = "employeeId", required = false) Long employeeId,
                                   @RequestParam(value = "status", required = false) String status,
                                   @RequestParam(value = "from", required = false) String from,
                                   @RequestParam(value = "to", required = false) String to) {
        if (from != null && to != null) {
            LocalDateTime fromDate = LocalDateTime.parse(from, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            LocalDateTime toDate = LocalDateTime.parse(to, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            
            if (employeeId != null) {
                return shiftRepository.findByEmployeeAndDateRange(employeeId, fromDate, toDate);
            }
            return shiftRepository.findByDateRange(fromDate, toDate);
        }
        
        if (employeeId != null) {
            return userService.findByUsername("employee").map(user -> 
                shiftRepository.findByEmployee(user)
            ).orElse(List.of());
        }
        
        if (status != null) {
            return shiftRepository.findByStatus(Shift.ShiftStatus.valueOf(status.toUpperCase()));
        }
        
        return shiftRepository.findAll();
    }

    @GetMapping("/my-shifts")
    public List<Shift> getMyShifts(Authentication authentication,
                                  @RequestParam(value = "from", required = false) String from,
                                  @RequestParam(value = "to", required = false) String to) {
        User user = (User) authentication.getPrincipal();
        
        if (from != null && to != null) {
            LocalDateTime fromDate = LocalDateTime.parse(from, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            LocalDateTime toDate = LocalDateTime.parse(to, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            return shiftRepository.findByEmployeeAndDateRange(user.getId(), fromDate, toDate);
        }
        
        return shiftRepository.findByEmployee(user);
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Map<String, Object> getShiftStats(@RequestParam(value = "days", defaultValue = "30") int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        
        Long activeShifts = shiftRepository.countActiveShifts();
        BigDecimal totalSales = shiftRepository.getTotalSalesByDateRange(startDate);
        List<Object[]> employeeStats = shiftRepository.getEmployeePerformanceStats(startDate);
        
        return Map.of(
                "activeShifts", activeShifts != null ? activeShifts : 0L,
                "totalSales", totalSales != null ? totalSales : BigDecimal.ZERO,
                "employeePerformance", employeeStats,
                "period", days + " days"
        );
    }

    public record ClockInRequest(String location, String notes) {}
    public record ClockOutRequest(BigDecimal salesAmount, Integer ordersProcessed, String notes) {}
}
