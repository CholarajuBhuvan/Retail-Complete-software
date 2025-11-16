package in.bhuvan.billingsoftware.service;

import in.bhuvan.billingsoftware.domain.Customer;
import in.bhuvan.billingsoftware.domain.CustomerOrder;
import in.bhuvan.billingsoftware.domain.OrderItem;
import in.bhuvan.billingsoftware.domain.Product;
import in.bhuvan.billingsoftware.domain.User;
import in.bhuvan.billingsoftware.repo.CustomerOrderRepository;
import in.bhuvan.billingsoftware.repo.CustomerRepository;
import in.bhuvan.billingsoftware.repo.ProductRepository;
import in.bhuvan.billingsoftware.repo.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@SuppressWarnings("null")
public class OrderService {

    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final CustomerOrderRepository orderRepository;
    private final UserRepository userRepository;

    @org.springframework.beans.factory.annotation.Value("${app.tax.percent:10}")
    private java.math.BigDecimal taxPercent;

    public OrderService(CustomerRepository customerRepository,
                        ProductRepository productRepository,
                        CustomerOrderRepository orderRepository,
                        UserRepository userRepository) {
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public CustomerOrder createOrder(Long customerId, List<ItemRequest> items, java.math.BigDecimal discountPercent) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        // Get current logged-in user
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username).orElse(null);

        CustomerOrder order = new CustomerOrder();
        order.setCustomer(customer);
        order.setProcessedBy(currentUser);
        order.setCreatedAt(OffsetDateTime.now());

        BigDecimal subtotal = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (ItemRequest req : items) {
            Product product = productRepository.findById(req.productId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + req.productId()));

            if (product.getStockQuantity() < req.quantity()) {
                throw new IllegalStateException("Insufficient stock for product: " + product.getName());
            }

            product.setStockQuantity(product.getStockQuantity() - req.quantity());
            productRepository.save(product);

            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setProduct(product);
            oi.setQuantity(req.quantity());
            oi.setUnitPrice(product.getPrice());
            oi.setLineTotal(product.getPrice().multiply(BigDecimal.valueOf(req.quantity())));
            orderItems.add(oi);

            subtotal = subtotal.add(oi.getLineTotal());
        }

        order.setItems(orderItems);

        BigDecimal appliedDiscount = BigDecimal.ZERO;
        if (discountPercent != null && discountPercent.signum() > 0) {
            if (discountPercent.compareTo(new BigDecimal("100")) > 0) {
                throw new IllegalArgumentException("discountPercent cannot exceed 100");
            }
            appliedDiscount = subtotal.multiply(discountPercent).divide(new BigDecimal("100"));
            subtotal = subtotal.subtract(appliedDiscount);
            if (subtotal.signum() < 0) subtotal = BigDecimal.ZERO;
        }

        BigDecimal taxRate = (taxPercent != null ? taxPercent : new BigDecimal("10"));
        BigDecimal tax = subtotal.multiply(taxRate).divide(new BigDecimal("100"));
        BigDecimal total = subtotal.add(tax);
        order.setSubtotal(subtotal);
        order.setTax(tax);
        order.setTotal(total);

        CustomerOrder savedOrder = orderRepository.save(order);
        System.out.println("=== ORDER SAVED ===");
        System.out.println("Order ID: " + savedOrder.getId());
        System.out.println("Created At: " + savedOrder.getCreatedAt());
        System.out.println("Total: " + savedOrder.getTotal());
        System.out.println("==================");
        return savedOrder;
    }

    public record ItemRequest(Long productId, int quantity) {}
}


