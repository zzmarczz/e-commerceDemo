package com.demo.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
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
    public Mono<ResponseEntity<String>> getAllProducts() {
        return webClientBuilder.build()
                .get()
                .uri(productServiceUrl + "/api/products")
                .retrieve()
                .toEntity(String.class)
                .map(response -> ResponseEntity.status(response.getStatusCode()).body(response.getBody()))
                .onErrorResume(WebClientResponseException.class, ex -> {
                    return Mono.just(ResponseEntity
                            .status(ex.getStatusCode())
                            .body(ex.getResponseBodyAsString()));
                });
    }

    @GetMapping("/products/{id}")
    public Mono<ResponseEntity<String>> getProduct(@PathVariable Long id) {
        return webClientBuilder.build()
                .get()
                .uri(productServiceUrl + "/api/products/" + id)
                .retrieve()
                .toEntity(String.class)
                .map(response -> ResponseEntity.status(response.getStatusCode()).body(response.getBody()))
                .onErrorResume(WebClientResponseException.class, ex -> {
                    return Mono.just(ResponseEntity
                            .status(ex.getStatusCode())
                            .body(ex.getResponseBodyAsString()));
                });
    }

    // Cart Service Endpoints
    @GetMapping("/cart/{userId}")
    public Mono<ResponseEntity<String>> getCart(@PathVariable String userId) {
        return webClientBuilder.build()
                .get()
                .uri(cartServiceUrl + "/api/cart/" + userId)
                .retrieve()
                .toEntity(String.class)
                .map(response -> ResponseEntity.status(response.getStatusCode()).body(response.getBody()))
                .onErrorResume(WebClientResponseException.class, ex -> {
                    return Mono.just(ResponseEntity
                            .status(ex.getStatusCode())
                            .body(ex.getResponseBodyAsString()));
                });
    }

    @PostMapping("/cart/{userId}/items")
    public Mono<ResponseEntity<String>> addToCart(@PathVariable String userId, @RequestBody String requestBody) {
        return webClientBuilder.build()
                .post()
                .uri(cartServiceUrl + "/api/cart/" + userId + "/items")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .toEntity(String.class)
                .map(response -> ResponseEntity.status(response.getStatusCode()).body(response.getBody()))
                .onErrorResume(WebClientResponseException.class, ex -> {
                    // Pass through client errors (4xx) and server errors (5xx) with original status and body
                    return Mono.just(ResponseEntity
                            .status(ex.getStatusCode())
                            .body(ex.getResponseBodyAsString()));
                });
    }

    @DeleteMapping("/cart/{userId}/items/{itemId}")
    public Mono<ResponseEntity<String>> removeFromCart(@PathVariable String userId, @PathVariable Long itemId) {
        return webClientBuilder.build()
                .delete()
                .uri(cartServiceUrl + "/api/cart/" + userId + "/items/" + itemId)
                .retrieve()
                .toEntity(String.class)
                .map(response -> ResponseEntity.status(response.getStatusCode()).body(response.getBody()))
                .onErrorResume(WebClientResponseException.class, ex -> {
                    return Mono.just(ResponseEntity
                            .status(ex.getStatusCode())
                            .body(ex.getResponseBodyAsString()));
                });
    }

    @DeleteMapping("/cart/{userId}")
    public Mono<ResponseEntity<String>> clearCart(@PathVariable String userId) {
        return webClientBuilder.build()
                .delete()
                .uri(cartServiceUrl + "/api/cart/" + userId)
                .retrieve()
                .toEntity(String.class)
                .map(response -> ResponseEntity.status(response.getStatusCode()).body(response.getBody()))
                .onErrorResume(WebClientResponseException.class, ex -> {
                    return Mono.just(ResponseEntity
                            .status(ex.getStatusCode())
                            .body(ex.getResponseBodyAsString()));
                });
    }

    // APM Funnel Tracking: Cart View Event
    @PostMapping("/cart/{userId}/view-event")
    public Mono<ResponseEntity<String>> trackCartView(@PathVariable String userId,
                                                      @RequestHeader(value = "X-Session-ID", required = false) String sessionId,
                                                      @RequestHeader(value = "X-Journey-ID", required = false) String journeyId) {
        WebClient.RequestHeadersSpec<?> request = webClientBuilder.build()
                .post()
                .uri(cartServiceUrl + "/api/cart/" + userId + "/view-event");
        
        if (sessionId != null) {
            request = request.header("X-Session-ID", sessionId);
        }
        if (journeyId != null) {
            request = request.header("X-Journey-ID", journeyId);
        }
        
        return request
                .retrieve()
                .toEntity(String.class)
                .map(response -> ResponseEntity.status(response.getStatusCode()).body(response.getBody()))
                .onErrorResume(WebClientResponseException.class, ex -> {
                    return Mono.just(ResponseEntity
                            .status(ex.getStatusCode())
                            .body(ex.getResponseBodyAsString()));
                });
    }

    // APM Funnel Tracking: Checkout Initiated Event
    @PostMapping("/cart/{userId}/checkout-initiated")
    public Mono<ResponseEntity<String>> trackCheckoutInitiated(@PathVariable String userId,
                                                               @RequestHeader(value = "X-Session-ID", required = false) String sessionId,
                                                               @RequestHeader(value = "X-Journey-ID", required = false) String journeyId) {
        WebClient.RequestHeadersSpec<?> request = webClientBuilder.build()
                .post()
                .uri(cartServiceUrl + "/api/cart/" + userId + "/checkout-initiated");
        
        if (sessionId != null) {
            request = request.header("X-Session-ID", sessionId);
        }
        if (journeyId != null) {
            request = request.header("X-Journey-ID", journeyId);
        }
        
        return request
                .retrieve()
                .toEntity(String.class)
                .map(response -> ResponseEntity.status(response.getStatusCode()).body(response.getBody()))
                .onErrorResume(WebClientResponseException.class, ex -> {
                    return Mono.just(ResponseEntity
                            .status(ex.getStatusCode())
                            .body(ex.getResponseBodyAsString()));
                });
    }

    // Order Service Endpoints
    @PostMapping("/orders/checkout")
    public Mono<ResponseEntity<String>> checkout(@RequestBody String requestBody,
                                                 @RequestHeader(value = "X-Session-ID", required = false) String sessionId,
                                                 @RequestHeader(value = "X-Journey-ID", required = false) String journeyId) {
        WebClient.RequestBodySpec request = webClientBuilder.build()
                .post()
                .uri(orderServiceUrl + "/api/orders/checkout")
                .contentType(MediaType.APPLICATION_JSON);
        
        if (sessionId != null) {
            request = request.header("X-Session-ID", sessionId);
        }
        if (journeyId != null) {
            request = request.header("X-Journey-ID", journeyId);
        }
        
        return request
                .bodyValue(requestBody)
                .retrieve()
                .toEntity(String.class)
                .map(response -> ResponseEntity.status(response.getStatusCode()).body(response.getBody()))
                .onErrorResume(WebClientResponseException.class, ex -> {
                    // Pass through client errors (4xx) and server errors (5xx) with original status and body
                    return Mono.just(ResponseEntity
                            .status(ex.getStatusCode())
                            .body(ex.getResponseBodyAsString()));
                });
    }

    @GetMapping("/orders/user/{userId}")
    public Mono<ResponseEntity<String>> getUserOrders(@PathVariable String userId) {
        return webClientBuilder.build()
                .get()
                .uri(orderServiceUrl + "/api/orders/user/" + userId)
                .retrieve()
                .toEntity(String.class)
                .map(response -> ResponseEntity.status(response.getStatusCode()).body(response.getBody()))
                .onErrorResume(WebClientResponseException.class, ex -> {
                    return Mono.just(ResponseEntity
                            .status(ex.getStatusCode())
                            .body(ex.getResponseBodyAsString()));
                });
    }

    @GetMapping("/orders/{orderId}")
    public Mono<ResponseEntity<String>> getOrder(@PathVariable Long orderId) {
        return webClientBuilder.build()
                .get()
                .uri(orderServiceUrl + "/api/orders/" + orderId)
                .retrieve()
                .toEntity(String.class)
                .map(response -> ResponseEntity.status(response.getStatusCode()).body(response.getBody()))
                .onErrorResume(WebClientResponseException.class, ex -> {
                    return Mono.just(ResponseEntity
                            .status(ex.getStatusCode())
                            .body(ex.getResponseBodyAsString()));
                });
    }

    @GetMapping("/orders")
    public Mono<ResponseEntity<String>> getAllOrders() {
        return webClientBuilder.build()
                .get()
                .uri(orderServiceUrl + "/api/orders")
                .retrieve()
                .toEntity(String.class)
                .map(response -> ResponseEntity.status(response.getStatusCode()).body(response.getBody()))
                .onErrorResume(WebClientResponseException.class, ex -> {
                    return Mono.just(ResponseEntity
                            .status(ex.getStatusCode())
                            .body(ex.getResponseBodyAsString()));
                });
    }

    // Health check endpoint
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("API Gateway is running");
    }
}

