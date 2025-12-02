package com.demo.order.controller;

import com.demo.order.dto.CheckoutRequest;
import com.demo.order.model.Order;
import com.demo.order.model.OrderItem;
import com.demo.order.model.OrderStatus;
import com.demo.order.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private WebClient.Builder webClientBuilder;
    
    @Value("${services.cart.url:http://localhost:8082}")
    private String cartServiceUrl;
    
    // APM Demo: Simulate slow response times
    private static volatile boolean slowModeEnabled = false;
    private static volatile int slowModeDelayMs = 5000; // Default 5 seconds

    @PostMapping("/checkout")
    public ResponseEntity<Order> checkout(@RequestBody CheckoutRequest request,
                                         @RequestHeader(value = "X-Session-ID", required = false) String sessionId,
                                         @RequestHeader(value = "X-Journey-ID", required = false) String journeyId) {
        
        String userId = request.getUserId();
        int itemCount = request.getItems().size();
        
        logger.info("FUNNEL_TRACKING: Checkout started - userId={}, sessionId={}, journeyId={}, items={}", 
                    userId, sessionId, journeyId, itemCount);
        
        // Stage 1: Validate cart
        logger.info("FUNNEL_STAGE: Validating cart - userId={}", userId);
        if (request.getItems() == null || request.getItems().isEmpty()) {
            logger.warn("FUNNEL_DROP_OFF: Checkout validation failed - empty cart - userId={}, sessionId={}", 
                       userId, sessionId);
            return ResponseEntity.badRequest().build();
        }
        
        logger.info("FUNNEL_STAGE: Cart validated successfully - userId={}, items={}", userId, itemCount);
        
        // Stage 2: Calculate total
        logger.info("FUNNEL_STAGE: Calculating order total - userId={}", userId);
        double total = request.getItems().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
        
        logger.info("FUNNEL_METRICS: Order total calculated - userId={}, totalValue=${}", 
                    userId, String.format("%.2f", total));

        // Stage 3: Create order
        logger.info("FUNNEL_STAGE: Creating order - userId={}", userId);
        Order order = new Order(userId, total);

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
        
        logger.info("FUNNEL_STAGE: Order created - orderId={}, userId={}", savedOrder.getId(), userId);
        
        // Stage 4: Clear cart after successful checkout (with retry for concurrency)
        logger.info("FUNNEL_STAGE: Clearing cart after successful checkout - userId={}", userId);
        clearCartWithRetry(userId);
        
        // Stage 5: Checkout completed
        logger.info("FUNNEL_TRACKING: Checkout completed successfully - orderId={}, userId={}, sessionId={}, journeyId={}, totalValue=${}", 
                    savedOrder.getId(), userId, sessionId, journeyId, String.format("%.2f", total));
        
        // Add revenue tracking headers for APM (X- prefix for better APM compatibility)
        return ResponseEntity.ok()
                .header("X-Order-Id", savedOrder.getId().toString())
                .header("X-Order-Value", String.format("%.2f", total))
                .header("X-Item-Count", String.valueOf(itemCount))
                .body(savedOrder);
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
    
    // APM Revenue Tracking: Metrics endpoint
    @GetMapping("/metrics/revenue")
    public ResponseEntity<Map<String, Object>> getRevenueMetrics() {
        List<Order> allOrders = orderRepository.findAll();
        
        double totalRevenue = allOrders.stream()
                .mapToDouble(Order::getTotalAmount)
                .sum();
        
        double averageOrderValue = allOrders.isEmpty() ? 0 : totalRevenue / allOrders.size();
        
        long totalOrders = allOrders.size();
        
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalRevenue", String.format("%.2f", totalRevenue));
        metrics.put("totalOrders", totalOrders);
        metrics.put("averageOrderValue", String.format("%.2f", averageOrderValue));
        metrics.put("timestamp", java.time.Instant.now().toString());
        
        logger.info("REVENUE_METRICS: totalRevenue=${}, totalOrders={}, avgOrderValue=${}", 
                   String.format("%.2f", totalRevenue), totalOrders, String.format("%.2f", averageOrderValue));
        
        return ResponseEntity.ok(metrics);
    }
    
    // Helper method: Clear cart with retry logic for optimistic locking failures
    private void clearCartWithRetry(String userId) {
        int maxRetries = 3;
        int retryDelay = 100; // milliseconds
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                webClientBuilder.build()
                        .delete()
                        .uri(cartServiceUrl + "/api/cart/" + userId)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
                
                logger.info("FUNNEL_STAGE: Cart cleared successfully - userId={}, attempt={}", userId, attempt);
                return; // Success, exit
                
            } catch (Exception e) {
                String errorMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
                
                if (attempt < maxRetries && (errorMsg.contains("OptimisticLocking") || errorMsg.contains("StaleObject"))) {
                    logger.warn("FUNNEL_RETRY: Optimistic locking failure clearing cart - userId={}, attempt={}/{}, retrying in {}ms", 
                               userId, attempt, maxRetries, retryDelay);
                    try {
                        Thread.sleep(retryDelay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    logger.error("FUNNEL_ERROR: Failed to clear cart after checkout - userId={}, attempt={}/{}, error={}", 
                                userId, attempt, maxRetries, errorMsg);
                    // Don't fail the checkout if cart clearing fails
                    return;
                }
            }
        }
    }
}

