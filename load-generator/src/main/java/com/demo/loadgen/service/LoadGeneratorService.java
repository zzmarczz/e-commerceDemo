package com.demo.loadgen.service;

import com.demo.loadgen.model.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class LoadGeneratorService {

    private final WebClient webClient;
    
    @Value("${gateway.url:http://localhost:8080}")
    private String gatewayUrl;
    
    @Value("${load.users:20}")
    private int numberOfUsers;
    
    @Value("${load.enabled:true}")
    private boolean loadEnabled;
    
    @Value("${load.intensity:medium}")
    private String loadIntensity;
    
    // Statistics
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong successfulRequests = new AtomicLong(0);
    private final AtomicLong failedRequests = new AtomicLong(0);
    private final AtomicLong totalResponseTime = new AtomicLong(0);
    
    private final Map<String, AtomicInteger> actionCounts = new HashMap<>();
    
    public LoadGeneratorService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
        initializeActionCounts();
    }
    
    private void initializeActionCounts() {
        actionCounts.put("browse_products", new AtomicInteger(0));
        actionCounts.put("view_product", new AtomicInteger(0));
        actionCounts.put("add_to_cart", new AtomicInteger(0));
        actionCounts.put("view_cart", new AtomicInteger(0));
        actionCounts.put("checkout", new AtomicInteger(0));
        actionCounts.put("view_orders", new AtomicInteger(0));
        actionCounts.put("view_all_orders", new AtomicInteger(0));
    }

    // High frequency - every 2 seconds
    @Scheduled(fixedDelay = 2000)
    public void generateHighFrequencyLoad() {
        if (!loadEnabled) return;
        
        int concurrency = getIntensityConcurrency();
        
        Flux.range(0, concurrency)
            .flatMap(i -> simulateUserJourney())
            .subscribe(
                result -> {},
                error -> System.err.println("Error in load generation: " + error.getMessage())
            );
    }
    
    // Medium frequency - every 5 seconds
    @Scheduled(fixedDelay = 5000)
    public void generateCartOperations() {
        if (!loadEnabled) return;
        
        Flux.range(0, 3)
            .flatMap(i -> {
                String userId = getRandomUser();
                return browseAndAddToCart(userId);
            })
            .subscribe();
    }
    
    // Low frequency - every 10 seconds
    @Scheduled(fixedDelay = 10000)
    public void generateCheckouts() {
        if (!loadEnabled) return;
        
        Flux.range(0, 2)
            .flatMap(i -> {
                String userId = getRandomUser();
                return completeCheckoutJourney(userId);
            })
            .subscribe();
    }
    
    // Administrative operations - every 15 seconds
    @Scheduled(fixedDelay = 15000)
    public void generateAdminOperations() {
        if (!loadEnabled) return;
        
        viewAllOrders().subscribe();
        
        // View multiple specific orders
        Flux.range(1, 5)
            .flatMap(orderId -> viewOrder(orderId.longValue()))
            .subscribe();
    }
    
    // Print statistics - every 30 seconds
    @Scheduled(fixedDelay = 30000)
    public void printStatistics() {
        if (totalRequests.get() == 0) return;
        
        long total = totalRequests.get();
        long successful = successfulRequests.get();
        long failed = failedRequests.get();
        long avgResponseTime = total > 0 ? totalResponseTime.get() / total : 0;
        double successRate = total > 0 ? (successful * 100.0 / total) : 0;
        
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║         Load Generator Stats           ║");
        System.out.println("╚════════════════════════════════════════╝");
        System.out.println("Total Requests: " + total);
        System.out.println("Successful: " + successful + " (" + String.format("%.2f", successRate) + "%)");
        System.out.println("Failed: " + failed);
        System.out.println("Avg Response Time: " + avgResponseTime + "ms");
        System.out.println("\nAction Breakdown:");
        actionCounts.forEach((action, count) -> 
            System.out.println("  " + action + ": " + count.get())
        );
        System.out.println("════════════════════════════════════════\n");
    }
    
    private Mono<String> simulateUserJourney() {
        String userId = getRandomUser();
        int journeyType = ThreadLocalRandom.current().nextInt(5);
        
        return switch (journeyType) {
            case 0 -> quickBrowseJourney(userId);
            case 1 -> browseAndAddToCart(userId);
            case 2 -> completeCheckoutJourney(userId);
            case 3 -> returningCustomerJourney(userId);
            default -> productExplorationJourney(userId);
        };
    }
    
    private Mono<String> quickBrowseJourney(String userId) {
        return browseProducts()
            .then(viewProduct(getRandomProductId()))
            .then(viewProduct(getRandomProductId()))
            .then(Mono.just("Quick browse completed"));
    }
    
    private Mono<String> browseAndAddToCart(String userId) {
        return browseProducts()
            .then(viewProduct(getRandomProductId()))
            .then(addToCart(userId, getRandomProductId(), getRandomQuantity()))
            .then(addToCart(userId, getRandomProductId(), getRandomQuantity()))
            .then(viewCart(userId))
            .then(Mono.just("Browse and add to cart completed"));
    }
    
    private Mono<String> completeCheckoutJourney(String userId) {
        Long productId1 = getRandomProductId();
        Long productId2 = getRandomProductId();
        
        return browseProducts()
            .then(viewProduct(productId1))
            .then(addToCart(userId, productId1, getRandomQuantity()))
            .then(addToCart(userId, productId2, getRandomQuantity()))
            .then(viewCart(userId))
            .then(checkout(userId, Arrays.asList(
                new CheckoutItem(productId1, "Product" + productId1, 99.99, 1),
                new CheckoutItem(productId2, "Product" + productId2, 79.99, 1)
            )))
            .then(viewOrders(userId))
            .then(Mono.just("Complete checkout journey completed"));
    }
    
    private Mono<String> returningCustomerJourney(String userId) {
        return viewOrders(userId)
            .then(browseProducts())
            .then(viewProduct(getRandomProductId()))
            .then(addToCart(userId, getRandomProductId(), getRandomQuantity()))
            .then(viewCart(userId))
            .then(Mono.just("Returning customer journey completed"));
    }
    
    private Mono<String> productExplorationJourney(String userId) {
        return browseProducts()
            .then(viewProduct(1L))
            .then(viewProduct(2L))
            .then(viewProduct(3L))
            .then(viewProduct(4L))
            .then(viewProduct(5L))
            .then(Mono.just("Product exploration completed"));
    }
    
    // API calls with metrics
    private Mono<String> browseProducts() {
        return makeRequest("browse_products",
            webClient.get()
                .uri(gatewayUrl + "/api/products")
                .retrieve()
                .bodyToMono(String.class)
        );
    }
    
    private Mono<String> viewProduct(Long productId) {
        return makeRequest("view_product",
            webClient.get()
                .uri(gatewayUrl + "/api/products/" + productId)
                .retrieve()
                .bodyToMono(String.class)
        );
    }
    
    private Mono<String> addToCart(String userId, Long productId, int quantity) {
        AddToCartRequest request = new AddToCartRequest(
            productId, "Product" + productId, 99.99, quantity
        );
        
        return makeRequest("add_to_cart",
            webClient.post()
                .uri(gatewayUrl + "/api/cart/" + userId + "/items")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
        );
    }
    
    private Mono<String> viewCart(String userId) {
        return makeRequest("view_cart",
            webClient.get()
                .uri(gatewayUrl + "/api/cart/" + userId)
                .retrieve()
                .bodyToMono(String.class)
        );
    }
    
    private Mono<String> checkout(String userId, List<CheckoutItem> items) {
        CheckoutRequest request = new CheckoutRequest(userId, items);
        
        return makeRequest("checkout",
            webClient.post()
                .uri(gatewayUrl + "/api/orders/checkout")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
        );
    }
    
    private Mono<String> viewOrders(String userId) {
        return makeRequest("view_orders",
            webClient.get()
                .uri(gatewayUrl + "/api/orders/user/" + userId)
                .retrieve()
                .bodyToMono(String.class)
        );
    }
    
    private Mono<String> viewAllOrders() {
        return makeRequest("view_all_orders",
            webClient.get()
                .uri(gatewayUrl + "/api/orders")
                .retrieve()
                .bodyToMono(String.class)
        );
    }
    
    private Mono<String> viewOrder(Long orderId) {
        return makeRequest("view_order",
            webClient.get()
                .uri(gatewayUrl + "/api/orders/" + orderId)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(e -> Mono.just("Order not found"))
        );
    }
    
    private Mono<String> makeRequest(String action, Mono<String> request) {
        long startTime = System.currentTimeMillis();
        
        return request
            .doOnSuccess(result -> {
                long responseTime = System.currentTimeMillis() - startTime;
                totalRequests.incrementAndGet();
                successfulRequests.incrementAndGet();
                totalResponseTime.addAndGet(responseTime);
                actionCounts.get(action).incrementAndGet();
            })
            .doOnError(error -> {
                totalRequests.incrementAndGet();
                failedRequests.incrementAndGet();
            })
            .onErrorResume(e -> Mono.just("Error"))
            .timeout(Duration.ofSeconds(5));
    }
    
    // Helper methods
    private String getRandomUser() {
        return "user" + ThreadLocalRandom.current().nextInt(1, numberOfUsers + 1);
    }
    
    private Long getRandomProductId() {
        return ThreadLocalRandom.current().nextLong(1, 6);
    }
    
    private int getRandomQuantity() {
        return ThreadLocalRandom.current().nextInt(1, 4);
    }
    
    private int getIntensityConcurrency() {
        return switch (loadIntensity.toLowerCase()) {
            case "low" -> 1;
            case "high" -> 5;
            default -> 3; // medium
        };
    }
    
    // Public methods for control
    public void setLoadEnabled(boolean enabled) {
        this.loadEnabled = enabled;
        System.out.println("Load generation " + (enabled ? "enabled" : "disabled"));
    }
    
    public void setLoadIntensity(String intensity) {
        this.loadIntensity = intensity;
        System.out.println("Load intensity set to: " + intensity);
    }
    
    public LoadStats getStatistics() {
        return new LoadStats(
            totalRequests.get(),
            successfulRequests.get(),
            failedRequests.get(),
            totalRequests.get() > 0 ? totalResponseTime.get() / totalRequests.get() : 0,
            new HashMap<>(actionCounts)
        );
    }
    
    public void resetStatistics() {
        totalRequests.set(0);
        successfulRequests.set(0);
        failedRequests.set(0);
        totalResponseTime.set(0);
        actionCounts.values().forEach(counter -> counter.set(0));
        System.out.println("Statistics reset");
    }
}

