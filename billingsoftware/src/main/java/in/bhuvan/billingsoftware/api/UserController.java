package in.bhuvan.billingsoftware.api;

import in.bhuvan.billingsoftware.domain.User;
import in.bhuvan.billingsoftware.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<User> getAllUsers(@RequestParam(value = "q", required = false) String query) {
        if (query != null && !query.isBlank()) {
            return userService.searchUsers(query);
        }
        return userService.getAllUsers();
    }

    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<User> getActiveUsers() {
        return userService.getActiveUsers();
    }

    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<User> getUsersByRole(@PathVariable User.Role role) {
        return userService.getUsersByRole(role);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest request) {
        try {
            // MANAGER can only create ADMIN and CASHIER users
            if (request.role() != User.Role.ADMIN && request.role() != User.Role.CASHIER) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Managers can only create ADMIN and CASHIER users"
                ));
            }
            
            User user = userService.createUser(
                    request.username(),
                    request.password(),
                    request.fullName(),
                    request.email(),
                    request.phone(),
                    request.role()
            );
            return ResponseEntity.ok(Map.of("success", true, "userId", user.getId()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @PutMapping("/{id}/password")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> resetUserPassword(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String newPassword = request.get("password");
            if (newPassword == null || newPassword.length() < 6) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Password must be at least 6 characters"
                ));
            }
            
            boolean updated = userService.resetPassword(id, newPassword);
            if (updated) {
                return ResponseEntity.ok(Map.of("success", true));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Failed to update password"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('MANAGER') and #id != authentication.principal.id)")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest request) {
        try {
            userService.updateUser(id, request.fullName(), request.email(), 
                    request.phone(), request.role(), request.enabled());
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    public record CreateUserRequest(
            String username,
            String password,
            String fullName,
            String email,
            String phone,
            User.Role role
    ) {}

    public record UpdateUserRequest(
            String fullName,
            String email,
            String phone,
            User.Role role,
            Boolean enabled
    ) {}
}
