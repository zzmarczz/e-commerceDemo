package com.demo.cart.controller;

import com.demo.cart.dto.AddToCartRequest;
import com.demo.cart.model.Cart;
import com.demo.cart.model.CartItem;
import com.demo.cart.repository.CartRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private static final Logger logger = LoggerFactory.getLogger(CartController.class);

    @Autowired
    private CartRepository cartRepository;

    @GetMapping("/{userId}")
    public ResponseEntity<Cart> getCart(@PathVariable String userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> cartRepository.save(new Cart(userId)));
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/{userId}/items")
    public ResponseEntity<?> addToCart(@PathVariable String userId, 
                                         @RequestBody AddToCartRequest request) {
        // Find or create cart - if new, save it first
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart(userId);
                    return cartRepository.save(newCart);
                });

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
    }

    @DeleteMapping("/{userId}/items/{itemId}")
    public ResponseEntity<Cart> removeFromCart(@PathVariable String userId, 
                                               @PathVariable Long itemId) {
        return cartRepository.findByUserId(userId)
                .map(cart -> {
                    cart.getItems().removeIf(item -> item.getId().equals(itemId));
                    Cart savedCart = cartRepository.save(cart);
                    return ResponseEntity.ok(savedCart);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> clearCart(@PathVariable String userId) {
        return cartRepository.findByUserId(userId)
                .map(cart -> {
                    cart.getItems().clear();
                    cartRepository.save(cart);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // APM Funnel Tracking: Cart View Event
    @PostMapping("/{userId}/view-event")
    public ResponseEntity<Cart> trackCartView(@PathVariable String userId,
                                              @RequestHeader(value = "X-Session-ID", required = false) String sessionId,
                                              @RequestHeader(value = "X-Journey-ID", required = false) String journeyId) {
        logger.info("FUNNEL_TRACKING: Cart viewed - userId={}, sessionId={}, journeyId={}", 
                    userId, sessionId, journeyId);
        
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> cartRepository.save(new Cart(userId)));
        
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

