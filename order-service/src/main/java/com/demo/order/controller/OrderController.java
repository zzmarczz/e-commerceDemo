package com.demo.order.controller;

import com.demo.order.dto.CheckoutRequest;
import com.demo.order.model.Order;
import com.demo.order.model.OrderItem;
import com.demo.order.model.OrderStatus;
import com.demo.order.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;
    
    // APM Demo: Simulate slow response times
    private static volatile boolean slowModeEnabled = false;
    private static volatile int slowModeDelayMs = 5000; // Default 5 seconds

    @PostMapping("/checkout")
    public ResponseEntity<Order> checkout(@RequestBody CheckoutRequest request) {
        // Calculate total
        double total = request.getItems().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();

        Order order = new Order(request.getUserId(), total);

        // Add items to order
        request.getItems().forEach(item -> {
            OrderItem orderItem = new OrderItem(
                item.getProductId(),
                item.getProductName(),
                item.getPrice(),
                item.getQuantity()
            );
            order.addItem(orderItem);
        });

        order.setStatus(OrderStatus.CONFIRMED);
        Order savedOrder = orderRepository.save(order);
        
        return ResponseEntity.ok(savedOrder);
    }

    @GetMapping("/user/{userId}")
    public List<Order> getUserOrders(@PathVariable String userId) {
        return orderRepository.findByUserId(userId);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrder(@PathVariable Long orderId) {
        return orderRepository.findById(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<Order> getAllOrders() {
        // APM Demo: Simulate slow response time
        if (slowModeEnabled) {
            try {
                System.out.println("🐌 SLOW MODE: Delaying response by " + slowModeDelayMs + "ms for APM demo");
                Thread.sleep(slowModeDelayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return orderRepository.findAll();
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<Order> updateOrderStatus(@PathVariable Long orderId, 
                                                   @RequestParam OrderStatus status) {
        return orderRepository.findById(orderId)
                .map(order -> {
                    order.setStatus(status);
                    return ResponseEntity.ok(orderRepository.save(order));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    // APM Demo Control Endpoints
    @PostMapping("/control/slow-mode")
    public ResponseEntity<Map<String, Object>> setSlowMode(
            @RequestParam(required = false, defaultValue = "true") boolean enabled,
            @RequestParam(required = false, defaultValue = "5000") int delayMs) {
        
        slowModeEnabled = enabled;
        slowModeDelayMs = delayMs;
        
        Map<String, Object> response = new HashMap<>();
        response.put("slowModeEnabled", slowModeEnabled);
        response.put("delayMs", slowModeDelayMs);
        response.put("message", slowModeEnabled ? 
            "🐌 SLOW MODE ACTIVATED - /api/orders will be delayed by " + delayMs + "ms" :
            "✅ SLOW MODE DEACTIVATED - Normal response times restored");
        
        System.out.println(response.get("message"));
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/control/slow-mode")
    public ResponseEntity<Map<String, Object>> getSlowModeStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("slowModeEnabled", slowModeEnabled);
        response.put("delayMs", slowModeDelayMs);
        return ResponseEntity.ok(response);
    }
}

