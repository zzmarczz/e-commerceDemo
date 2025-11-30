package com.demo.cart.controller;

import com.demo.cart.dto.AddToCartRequest;
import com.demo.cart.model.Cart;
import com.demo.cart.model.CartItem;
import com.demo.cart.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

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
}

