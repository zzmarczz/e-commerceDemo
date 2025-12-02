package com.demo.loadgen.service;

import com.demo.loadgen.model.*;
import jakarta.annotation.PostConstruct;
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
    
    // APM Funnel Tracking: Session IDs for each simulated user
    private final Map<String, String> userSessions = new HashMap<>();
    
    // User Personas: Each user has a fixed behavior type for realistic funnel
    private final Map<String, String> userPersonas = new HashMap<>();
    
    // Persona types
    private static final String PERSONA_BROWSE_ONLY = "browse_only";
    private static final String PERSONA_ADD_ABANDON = "add_abandon";
    private static final String PERSONA_VIEW_ABANDON = "view_abandon";
    private static final String PERSONA_BUYER = "buyer";
    
    public LoadGeneratorService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
        initializeActionCounts();
    }
    
    @PostConstruct
    private void initializeUserSessions() {
        // Pre-create session IDs and assign personas for realistic funnel
        // This runs AFTER @Value injection, so numberOfUsers is available
        System.out.println("Initializing " + numberOfUsers + " user sessions and personas for APM tracking...");
        
        // Calculate persona distribution (40% browse, 20% add-abandon, 15% view-abandon, 25% buyer)
        int browseOnlyCount = (int)(numberOfUsers * 0.40);
        int addAbandonCount = (int)(numberOfUsers * 0.20);
        int viewAbandonCount = (int)(numberOfUsers * 0.15);
        // Remaining users are buyers
        
        for (int i = 1; i <= numberOfUsers; i++) {
            String userId = "user" + i;
            String sessionId = "loadgen-session-" + System.currentTimeMillis() + "-" + i;
            userSessions.put(userId, sessionId);
            
            // Assign persona based on user number
            String persona;
            if (i <= browseOnlyCount) {
                persona = PERSONA_BROWSE_ONLY;
            } else if (i <= browseOnlyCount + addAbandonCount) {
                persona = PERSONA_ADD_ABANDON;
            } else if (i <= browseOnlyCount + addAbandonCount + viewAbandonCount) {
                persona = PERSONA_VIEW_ABANDON;
            } else {
                persona = PERSONA_BUYER;
            }
            userPersonas.put(userId, persona);
        }
        
        // Add admin user session for administrative operations
        userSessions.put("admin", "loadgen-session-admin-" + System.currentTimeMillis());
        
        // Print distribution
        System.out.println("User sessions initialized: " + userSessions.size() + " sessions created");
        System.out.println("Persona distribution:");
        System.out.println("  - Browse only (40%): " + browseOnlyCount + " users (user1-user" + browseOnlyCount + ")");
        System.out.println("  - Add & abandon (20%): " + addAbandonCount + " users (user" + (browseOnlyCount+1) + "-user" + (browseOnlyCount+addAbandonCount) + ")");
        System.out.println("  - View cart & abandon (15%): " + viewAbandonCount + " users (user" + (browseOnlyCount+addAbandonCount+1) + "-user" + (browseOnlyCount+addAbandonCount+viewAbandonCount) + ")");
        System.out.println("  - Buyers (25%): " + (numberOfUsers - browseOnlyCount - addAbandonCount - viewAbandonCount) + " users (user" + (browseOnlyCount+addAbandonCount+viewAbandonCount+1) + "-user" + numberOfUsers + ")");
    }
    
    private String getSessionId(String userId) {
        return userSessions.getOrDefault(userId, "loadgen-session-unknown");
    }
    
    private String generateJourneyId() {
        return "loadgen-journey-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);
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
    // Generates journeys respecting user personas
    @Scheduled(fixedDelay = 5000)
    public void generateCartOperations() {
        if (!loadEnabled) return;
        
        Flux.range(0, 3)
            .flatMap(i -> simulateUserJourney())  // Uses persona system ✅
            .subscribe();
    }
    
    // Low frequency - every 10 seconds
    // Generates journeys respecting user personas
    @Scheduled(fixedDelay = 10000)
    public void generateCheckouts() {
        if (!loadEnabled) return;
        
        Flux.range(0, 2)
            .flatMap(i -> simulateUserJourney())  // Uses persona system ✅
            .subscribe();
    }
    
    // Administrative operations - every 15 seconds
    @Scheduled(fixedDelay = 15000)
    public void generateAdminOperations() {
        if (!loadEnabled) return;
        
        String adminUser = "admin";
        String journeyId = generateJourneyId();
        
        viewAllOrders(adminUser, journeyId).subscribe();
        
        // View multiple specific orders
        Flux.range(1, 5)
            .flatMap(orderId -> viewOrder(adminUser, journeyId, orderId.longValue()))
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
        String journeyId = generateJourneyId();
        
        // Get user's assigned persona and execute corresponding journey
        String persona = userPersonas.getOrDefault(userId, PERSONA_BROWSE_ONLY);
        
        return switch (persona) {
            case PERSONA_BROWSE_ONLY -> browseOnlyJourney(userId, journeyId);
            case PERSONA_ADD_ABANDON -> addToCartAndAbandon(userId, journeyId);
            case PERSONA_VIEW_ABANDON -> viewCartAndAbandon(userId, journeyId);
            case PERSONA_BUYER -> completeCheckoutJourney(userId, journeyId);
            default -> browseOnlyJourney(userId, journeyId);
        };
        
        // With persona-based behavior:
        // - Users 1-40: ALWAYS browse only (never add to cart)
        // - Users 41-60: ALWAYS add to cart and abandon
        // - Users 61-75: ALWAYS view cart and abandon
        // - Users 76-100: ALWAYS complete purchase
        //
        // Result: Consistent funnel regardless of timeframe!
        // Browse: 100 sessions, Add Cart: 60 sessions, View: 40 sessions, Purchase: 25 sessions
    }
    
    // REALISTIC DROP-OFF JOURNEYS FOR APM DEMO
    
    // 40% of users: Browse only, never add to cart (immediate drop-off)
    private Mono<String> browseOnlyJourney(String userId, String journeyId) {
        return browseProducts(userId, journeyId)
            .then(viewProduct(userId, journeyId, getRandomProductId()))
            .then(viewProduct(userId, journeyId, getRandomProductId()))
            .then(Mono.just("Browse only - dropped off"));
    }
    
    // 20% of users: Add to cart but abandon (don't view cart page)
    private Mono<String> addToCartAndAbandon(String userId, String journeyId) {
        return browseProducts(userId, journeyId)
            .then(viewProduct(userId, journeyId, getRandomProductId()))
            .then(addToCart(userId, journeyId, getRandomProductId(), getRandomQuantity()))
            .then(Mono.just("Added to cart - abandoned without viewing"));
    }
    
    // 15% of users: View cart but abandon before checkout
    private Mono<String> viewCartAndAbandon(String userId, String journeyId) {
        return browseProducts(userId, journeyId)
            .then(viewProduct(userId, journeyId, getRandomProductId()))
            .then(addToCart(userId, journeyId, getRandomProductId(), getRandomQuantity()))
            .then(addToCart(userId, journeyId, getRandomProductId(), getRandomQuantity()))
            .then(viewCart(userId, journeyId))
            .then(Mono.just("Viewed cart - abandoned before checkout"));
    }
    
    // LEGACY JOURNEYS (kept for scheduled operations)
    
    private Mono<String> quickBrowseJourney(String userId, String journeyId) {
        // Same as browseOnlyJourney
        return browseOnlyJourney(userId, journeyId);
    }
    
    private Mono<String> browseAndAddToCart(String userId, String journeyId) {
        // Same as viewCartAndAbandon
        return viewCartAndAbandon(userId, journeyId);
    }
    
    private Mono<String> completeCheckoutJourney(String userId, String journeyId) {
        Long productId1 = getRandomProductId();
        Long productId2 = getRandomProductId();
        
        return browseProducts(userId, journeyId)
            .then(viewProduct(userId, journeyId, productId1))
            .then(addToCart(userId, journeyId, productId1, getRandomQuantity()))
            .then(addToCart(userId, journeyId, productId2, getRandomQuantity()))
            .then(viewCart(userId, journeyId))
            .then(checkout(userId, journeyId, Arrays.asList(
                new CheckoutItem(productId1, "Product" + productId1, 99.99, 1),
                new CheckoutItem(productId2, "Product" + productId2, 79.99, 1)
            )))
            .then(viewOrders(userId, journeyId))
            .then(Mono.just("Complete checkout journey completed"));
    }
    
    private Mono<String> returningCustomerJourney(String userId, String journeyId) {
        // Returning customer views old orders then browses (often doesn't buy)
        return viewOrders(userId, journeyId)
            .then(browseProducts(userId, journeyId))
            .then(viewProduct(userId, journeyId, getRandomProductId()))
            .then(Mono.just("Returning customer - browsed only"));
    }
    
    private Mono<String> productExplorationJourney(String userId, String journeyId) {
        // Window shopping - browse multiple products but don't buy
        return browseProducts(userId, journeyId)
            .then(viewProduct(userId, journeyId, 1L))
            .then(viewProduct(userId, journeyId, 2L))
            .then(viewProduct(userId, journeyId, 3L))
            .then(Mono.just("Product exploration - no purchase"));
    }
    
    // API calls with metrics
    private Mono<String> browseProducts(String userId, String journeyId) {
        String sessionId = getSessionId(userId);
        
        return makeRequest("browse_products",
            webClient.get()
                .uri(gatewayUrl + "/api/products")
                .header("X-Session-ID", sessionId)
                .header("X-Journey-ID", journeyId)
                .retrieve()
                .bodyToMono(String.class)
        );
    }
    
    private Mono<String> viewProduct(String userId, String journeyId, Long productId) {
        String sessionId = getSessionId(userId);
        
        return makeRequest("view_product",
            webClient.get()
                .uri(gatewayUrl + "/api/products/" + productId)
                .header("X-Session-ID", sessionId)
                .header("X-Journey-ID", journeyId)
                .retrieve()
                .bodyToMono(String.class)
        );
    }
    
    private Mono<String> addToCart(String userId, String journeyId, Long productId, int quantity) {
        AddToCartRequest request = new AddToCartRequest(
            productId, "Product" + productId, 99.99, quantity
        );
        
        String sessionId = getSessionId(userId);
        
        return makeRequest("add_to_cart",
            webClient.post()
                .uri(gatewayUrl + "/api/cart/" + userId + "/items")
                .header("X-Session-ID", sessionId)
                .header("X-Journey-ID", journeyId)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
        );
    }
    
    private Mono<String> viewCart(String userId, String journeyId) {
        String sessionId = getSessionId(userId);
        
        return makeRequest("view_cart",
            webClient.get()
                .uri(gatewayUrl + "/api/cart/" + userId)
                .header("X-Session-ID", sessionId)
                .header("X-Journey-ID", journeyId)
                .retrieve()
                .bodyToMono(String.class)
        );
    }
    
    private Mono<String> checkout(String userId, String journeyId, List<CheckoutItem> items) {
        CheckoutRequest request = new CheckoutRequest(userId, items);
        String sessionId = getSessionId(userId);
        
        return makeRequest("checkout",
            webClient.post()
                .uri(gatewayUrl + "/api/orders/checkout")
                .header("X-Session-ID", sessionId)
                .header("X-Journey-ID", journeyId)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
        );
    }
    
    private Mono<String> viewOrders(String userId, String journeyId) {
        String sessionId = getSessionId(userId);
        
        return makeRequest("view_orders",
            webClient.get()
                .uri(gatewayUrl + "/api/orders/user/" + userId)
                .header("X-Session-ID", sessionId)
                .header("X-Journey-ID", journeyId)
                .retrieve()
                .bodyToMono(String.class)
        );
    }
    
    private Mono<String> viewAllOrders(String userId, String journeyId) {
        String sessionId = getSessionId(userId);
        
        return makeRequest("view_all_orders",
            webClient.get()
                .uri(gatewayUrl + "/api/orders")
                .header("X-Session-ID", sessionId)
                .header("X-Journey-ID", journeyId)
                .retrieve()
                .bodyToMono(String.class)
        );
    }
    
    private Mono<String> viewOrder(String userId, String journeyId, Long orderId) {
        String sessionId = getSessionId(userId);
        
        return makeRequest("view_order",
            webClient.get()
                .uri(gatewayUrl + "/api/orders/" + orderId)
                .header("X-Session-ID", sessionId)
                .header("X-Journey-ID", journeyId)
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


