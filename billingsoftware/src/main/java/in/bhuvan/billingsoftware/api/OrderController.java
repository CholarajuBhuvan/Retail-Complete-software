package in.bhuvan.billingsoftware.api;

import in.bhuvan.billingsoftware.domain.CustomerOrder;
import in.bhuvan.billingsoftware.repo.CustomerOrderRepository;
import in.bhuvan.billingsoftware.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/orders")
@SuppressWarnings("null")
public class OrderController {

    private final OrderService orderService;
    private final CustomerOrderRepository orderRepository;

    public OrderController(OrderService orderService, CustomerOrderRepository orderRepository) {
        this.orderService = orderService;
        this.orderRepository = orderRepository;
    }

    @PostMapping
    public ResponseEntity<CustomerOrder> create(@RequestBody CreateOrderRequest request) {
        try {
            System.out.println("=== ORDER CREATE REQUEST ===");
            System.out.println("Customer ID: " + request.customerId());
            System.out.println("Items count: " + request.items().size());
            System.out.println("Discount: " + request.discountPercent());
            
            CustomerOrder order = orderService.createOrder(request.customerId(), request.items(), request.discountPercent());
            
            System.out.println("Order created successfully with ID: " + order.getId());
            return ResponseEntity.created(URI.create("/api/orders/" + order.getId())).body(order);
        } catch (Exception e) {
            System.err.println("=== ORDER CREATION FAILED ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @GetMapping("/{id}")
    @SuppressWarnings("null")
    public ResponseEntity<CustomerOrder> getOrder(@PathVariable Long id) {
        return orderRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    public ResponseEntity<?> getAllOrders() {
        var orders = orderRepository.findAll();
        System.out.println("=== ALL ORDERS IN DATABASE ===");
        System.out.println("Total count: " + orders.size());
        orders.forEach(o -> {
            System.out.println("Order ID: " + o.getId() + ", Created: " + o.getCreatedAt() + ", Total: " + o.getTotal());
        });
        System.out.println("==============================");
        return ResponseEntity.ok(java.util.Map.of(
            "totalOrders", orders.size(),
            "orders", orders
        ));
    }

    public record CreateOrderRequest(Long customerId, java.util.List<OrderService.ItemRequest> items, java.math.BigDecimal discountPercent) {}
}


