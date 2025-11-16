package in.bhuvan.billingsoftware.bootstrap;

import in.bhuvan.billingsoftware.domain.User;
import in.bhuvan.billingsoftware.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class UserSeeder implements CommandLineRunner {

    private final UserService userService;

    @Override
    public void run(String... args) throws Exception {
        // Create default admin user if not exists
        if (userService.findByUsername("admin").isEmpty()) {
            userService.createUser(
                    "admin",
                    "admin123",
                    "System Administrator",
                    "admin@retail.com",
                    "+1234567890",
                    User.Role.ADMIN
            );
            log.info("Created default admin user: admin/admin123");
        }

        // Create default manager user if not exists
        if (userService.findByUsername("manager").isEmpty()) {
            userService.createUser(
                    "manager",
                    "manager123",
                    "Store Manager",
                    "manager@retail.com",
                    "+1234567891",
                    User.Role.MANAGER
            );
            log.info("Created default manager user: manager/manager123");
        }

        // Create default cashier user if not exists
        if (userService.findByUsername("cashier").isEmpty()) {
            userService.createUser(
                    "cashier",
                    "cashier123",
                    "Main Cashier",
                    "cashier@retail.com",
                    "+1234567892",
                    User.Role.CASHIER
            );
            log.info("Created default cashier user: cashier/cashier123");
        }

        // Create default employee user if not exists
        if (userService.findByUsername("employee").isEmpty()) {
            userService.createUser(
                    "employee",
                    "employee123",
                    "Store Employee",
                    "employee@retail.com",
                    "+1234567893",
                    User.Role.EMPLOYEE
            );
            log.info("Created default employee user: employee/employee123");
        }
    }
}
