package com.demo.cart.controller;

import com.demo.cart.dto.AddToCartRequest;
import com.demo.cart.model.Cart;
import com.demo.cart.model.CartItem;
import com.demo.cart.repository.CartRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private static final Logger logger = LoggerFactory.getLogger(CartController.class);

    @Autowired
    private CartRepository cartRepository;
    
    /**
     * Safely get or create a cart, handling race conditions
     * If multiple threads try to create a cart simultaneously, one will succeed
     * and the others will catch the constraint violation and retry the lookup
     */
    private Cart getOrCreateCart(String userId) {
        // First attempt: try to find existing cart
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    try {
                        // Not found, try to create new cart
                        Cart newCart = new Cart(userId);
                        return cartRepository.save(newCart);
                    } catch (DataIntegrityViolationException e) {
                        // Race condition: another thread created the cart
                        // between our check and save. Retry the lookup.
                        logger.debug("Race condition detected creating cart for userId={}, retrying lookup", userId);
                        return cartRepository.findByUserId(userId)
                                .orElseThrow(() -> new RuntimeException("Failed to get or create cart for user: " + userId));
                    }
                });
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Cart> getCart(@PathVariable String userId) {
        Cart cart = getOrCreateCart(userId);
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/{userId}/items")
    public ResponseEntity<?> addToCart(@PathVariable String userId, 
                                         @RequestBody AddToCartRequest request) {
        // Retry logic for optimistic locking conflicts
        int maxRetries = 3;
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                // Find or create cart (handles race conditions)
                Cart cart = getOrCreateCart(userId);

                // BUSINESS RULE: Check keyboard quantity limit (max 5 per customer)
                if (request.getProductName().equalsIgnoreCase("Keyboard")) {
                    int currentKeyboardCount = 0;
                    
                    // Count existing keyboards in cart
                    for (CartItem item : cart.getItems()) {
                        if (item.getProductName().equalsIgnoreCase("Keyboard")) {
                            currentKeyboardCount += item.getQuantity();
                        }
                    }
                    
                    // Calculate total after adding new quantity
                    int totalKeyboards = currentKeyboardCount + request.getQuantity();
                    
                    if (totalKeyboards > 5) {
                        // Throw error to showcase APM error detection
                        return ResponseEntity.badRequest()
                            .body("{\"error\":\"Business Rule Violation\",\"message\":\"Maximum 5 keyboards per customer. You currently have " 
                                + currentKeyboardCount + " keyboard(s) in cart. Cannot add " + request.getQuantity() + " more.\"}");
                    }
                }

                // BUG: Simulate a critical application error for APM demo (Monitor product)
                // This demonstrates how APM detects NullPointerException and stack traces
                if (request.getProductName().equalsIgnoreCase("Monitor")) {
                    // Intentionally trigger NullPointerException
                    String nullString = null;
                    // This will throw: java.lang.NullPointerException: Cannot invoke "String.length()" because "nullString" is null
                    int length = nullString.length();
                }

                // Check if item already exists
                boolean itemExists = false;
                for (CartItem item : cart.getItems()) {
                    if (item.getProductId().equals(request.getProductId())) {
                        item.setQuantity(item.getQuantity() + request.getQuantity());
                        itemExists = true;
                        break;
                    }
                }

                if (!itemExists) {
                    CartItem newItem = new CartItem(
                        request.getProductId(),
                        request.getProductName(),
                        request.getPrice(),
                        request.getQuantity()
                    );
                    cart.addItem(newItem);
                }

                Cart savedCart = cartRepository.save(cart);
                return ResponseEntity.ok(savedCart);
                
            } catch (ObjectOptimisticLockingFailureException e) {
                logger.warn("Optimistic locking failure adding to cart - userId={}, attempt={}/{}, retrying...", 
                           userId, attempt, maxRetries);
                
                if (attempt >= maxRetries) {
                    logger.error("Failed to add to cart after {} retries - userId={}", maxRetries, userId);
                    return ResponseEntity.status(500)
                        .body("{\"error\":\"Concurrency Error\",\"message\":\"Failed to add item due to concurrent modifications. Please try again.\"}");
                }
                
                // Small delay before retry
                try {
                    Thread.sleep(50 * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return ResponseEntity.status(500)
                        .body("{\"error\":\"Interrupted\",\"message\":\"Operation interrupted\"}");
                }
            }
        }
        
        return ResponseEntity.status(500)
            .body("{\"error\":\"Unknown\",\"message\":\"Failed to add to cart\"}");
    }

    @DeleteMapping("/{userId}/items/{itemId}")
    public ResponseEntity<?> removeFromCart(@PathVariable String userId, 
                                               @PathVariable Long itemId) {
        Optional<Cart> cartOpt = cartRepository.findByUserId(userId);
        if (!cartOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        Cart cart = cartOpt.get();
        cart.getItems().removeIf(item -> item.getId().equals(itemId));
        
        try {
            Cart savedCart = cartRepository.save(cart);
            return ResponseEntity.ok(savedCart);
        } catch (ObjectOptimisticLockingFailureException e) {
            logger.warn("Optimistic locking failure when removing from cart for userId: {}. Retrying operation.", userId);
            return ResponseEntity.status(409).body("{\"error\":\"Concurrency Issue\",\"message\":\"Cart was updated by another process. Please try again.\"}");
        }
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> clearCart(@PathVariable String userId) {
        // Retry logic for optimistic locking conflicts
        int maxRetries = 3;
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                Optional<Cart> cartOpt = cartRepository.findByUserId(userId);
                if (!cartOpt.isPresent()) {
                    return ResponseEntity.notFound().build();
                }
                
                Cart cart = cartOpt.get();
                cart.getItems().clear();
                cartRepository.save(cart);
                logger.debug("Cart cleared for userId={}, attempt={}", userId, attempt);
                return ResponseEntity.ok().build();
                        
            } catch (ObjectOptimisticLockingFailureException e) {
                logger.warn("Optimistic locking failure clearing cart - userId={}, attempt={}/{}, retrying...", 
                           userId, attempt, maxRetries);
                
                if (attempt >= maxRetries) {
                    logger.error("Failed to clear cart after {} retries - userId={}", maxRetries, userId);
                    return ResponseEntity.status(409)
                        .body("{\"error\":\"Concurrency Issue\",\"message\":\"Cart was updated by another process. Please try again.\"}");
                }
                
                // Small delay before retry
                try {
                    Thread.sleep(50 * attempt); // Increasing backoff: 50ms, 100ms, 150ms
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return ResponseEntity.status(500)
                        .body("{\"error\":\"Interrupted\",\"message\":\"Operation was interrupted.\"}");
                }
            }
        }
        
        return ResponseEntity.status(500)
            .body("{\"error\":\"Unknown\",\"message\":\"Failed to clear cart after retries.\"}");
    }

    // APM Funnel Tracking: Cart View Event
    @PostMapping("/{userId}/view-event")
    public ResponseEntity<Cart> trackCartView(@PathVariable String userId,
                                              @RequestHeader(value = "X-Session-ID", required = false) String sessionId,
                                              @RequestHeader(value = "X-Journey-ID", required = false) String journeyId) {
        logger.info("FUNNEL_TRACKING: Cart viewed - userId={}, sessionId={}, journeyId={}", 
                    userId, sessionId, journeyId);
        
        Cart cart = getOrCreateCart(userId);
        
        int itemCount = cart.getItems().size();
        double totalValue = cart.getItems().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
        
        logger.info("FUNNEL_METRICS: Cart viewed - items={}, totalValue=${}, userId={}", 
                    itemCount, String.format("%.2f", totalValue), userId);
        
        return ResponseEntity.ok(cart);
    }

    // APM Funnel Tracking: Checkout Initiated Event
    @PostMapping("/{userId}/checkout-initiated")
    public ResponseEntity<?> trackCheckoutInitiated(@PathVariable String userId,
                                                    @RequestHeader(value = "X-Session-ID", required = false) String sessionId,
                                                    @RequestHeader(value = "X-Journey-ID", required = false) String journeyId) {
        Cart cart = cartRepository.findByUserId(userId).orElse(null);
        
        if (cart == null || cart.getItems().isEmpty()) {
            logger.warn("FUNNEL_DROP_OFF: Checkout initiated with empty cart - userId={}, sessionId={}", 
                       userId, sessionId);
            return ResponseEntity.badRequest()
                    .body("{\"error\":\"Empty Cart\",\"message\":\"Cannot checkout with empty cart\"}");
        }
        
        int itemCount = cart.getItems().size();
        double totalValue = cart.getItems().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
        
        logger.info("FUNNEL_TRACKING: Checkout initiated - userId={}, sessionId={}, journeyId={}, items={}, totalValue=${}", 
                    userId, sessionId, journeyId, itemCount, String.format("%.2f", totalValue));
        
        return ResponseEntity.ok()
                .body("{\"status\":\"checkout_initiated\",\"items\":" + itemCount + ",\"totalValue\":" + totalValue + "}");
    }
}

