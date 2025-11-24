package com.demo.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class GatewayController {

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Value("${services.product.url}")
    private String productServiceUrl;

    @Value("${services.cart.url}")
    private String cartServiceUrl;

    @Value("${services.order.url}")
    private String orderServiceUrl;

    // Product Service Endpoints
    @GetMapping("/products")
    public Mono<String> getAllProducts() {
        return webClientBuilder.build()
                .get()
                .uri(productServiceUrl + "/api/products")
                .retrieve()
                .bodyToMono(String.class);
    }

    @GetMapping("/products/{id}")
    public Mono<String> getProduct(@PathVariable Long id) {
        return webClientBuilder.build()
                .get()
                .uri(productServiceUrl + "/api/products/" + id)
                .retrieve()
                .bodyToMono(String.class);
    }

    // Cart Service Endpoints
    @GetMapping("/cart/{userId}")
    public Mono<String> getCart(@PathVariable String userId) {
        return webClientBuilder.build()
                .get()
                .uri(cartServiceUrl + "/api/cart/" + userId)
                .retrieve()
                .bodyToMono(String.class);
    }

    @PostMapping("/cart/{userId}/items")
    public Mono<String> addToCart(@PathVariable String userId, @RequestBody String requestBody) {
        return webClientBuilder.build()
                .post()
                .uri(cartServiceUrl + "/api/cart/" + userId + "/items")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class);
    }

    @DeleteMapping("/cart/{userId}/items/{itemId}")
    public Mono<String> removeFromCart(@PathVariable String userId, @PathVariable Long itemId) {
        return webClientBuilder.build()
                .delete()
                .uri(cartServiceUrl + "/api/cart/" + userId + "/items/" + itemId)
                .retrieve()
                .bodyToMono(String.class);
    }

    @DeleteMapping("/cart/{userId}")
    public Mono<String> clearCart(@PathVariable String userId) {
        return webClientBuilder.build()
                .delete()
                .uri(cartServiceUrl + "/api/cart/" + userId)
                .retrieve()
                .bodyToMono(String.class);
    }

    // Order Service Endpoints
    @PostMapping("/orders/checkout")
    public Mono<String> checkout(@RequestBody String requestBody) {
        return webClientBuilder.build()
                .post()
                .uri(orderServiceUrl + "/api/orders/checkout")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class);
    }

    @GetMapping("/orders/user/{userId}")
    public Mono<String> getUserOrders(@PathVariable String userId) {
        return webClientBuilder.build()
                .get()
                .uri(orderServiceUrl + "/api/orders/user/" + userId)
                .retrieve()
                .bodyToMono(String.class);
    }

    @GetMapping("/orders/{orderId}")
    public Mono<String> getOrder(@PathVariable Long orderId) {
        return webClientBuilder.build()
                .get()
                .uri(orderServiceUrl + "/api/orders/" + orderId)
                .retrieve()
                .bodyToMono(String.class);
    }

    @GetMapping("/orders")
    public Mono<String> getAllOrders() {
        return webClientBuilder.build()
                .get()
                .uri(orderServiceUrl + "/api/orders")
                .retrieve()
                .bodyToMono(String.class);
    }

    // Health check endpoint
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("API Gateway is running");
    }
}

