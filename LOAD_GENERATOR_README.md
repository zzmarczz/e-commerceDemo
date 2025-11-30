# Load Generator Documentation

## Overview

The Load Generator is a Spring Boot application that generates continuous, realistic traffic to the e-commerce microservices for APM monitoring and performance testing.

## Quick Start

### Start with Load Generation

```bash
./start-with-load.sh
```

This starts all services + load generator on port 9090.

### Control Load Generation

```bash
# View statistics
curl http://localhost:9090/stats

# Enable load
curl -X POST http://localhost:9090/enable

# Disable load
curl -X POST http://localhost:9090/disable

# Set intensity (low, medium, high)
curl -X POST 'http://localhost:9090/intensity?level=high'

# Reset statistics
curl -X POST http://localhost:9090/reset
```

## Features

### 🎭 Realistic User Journeys

The load generator simulates 5 different user behaviors:

1. **Quick Browser** (20%)
   - Views multiple products
   - No cart interaction
   - Quick exit

2. **Window Shopper** (20%)
   - Browses products
   - Adds items to cart
   - Doesn't checkout

3. **Buyer** (20%)
   - Complete purchase journey
   - Browses → Adds to cart → Checkouts
   - Views order confirmation

4. **Returning Customer** (20%)
   - Checks order history first
   - Then browses
   - May add to cart

5. **Product Explorer** (20%)
   - Deep product browsing
   - Views multiple products
   - Detailed exploration

### 📊 Traffic Patterns

| Pattern | Frequency | Actions |
|---------|-----------|---------|
| High Frequency | Every 2s | User journeys |
| Medium Frequency | Every 5s | Cart operations |
| Low Frequency | Every 10s | Checkouts |
| Admin Operations | Every 15s | View all orders |
| Statistics | Every 30s | Print to console |

### 🎚️ Load Intensity Levels

| Level | Concurrent Users | Requests/min |
|-------|-----------------|--------------|
| Low | 1 | ~100 |
| Medium | 3 | ~300 |
| High | 5 | ~500+ |

## API Endpoints

### Statistics Endpoint

**GET** `/stats`

Returns current load generation statistics:

```json
{
  "totalRequests": 1250,
  "successfulRequests": 1248,
  "failedRequests": 2,
  "averageResponseTime": 45,
  "successRate": 99.84,
  "actionBreakdown": {
    "browse_products": 245,
    "view_product": 312,
    "add_to_cart": 198,
    "view_cart": 156,
    "checkout": 89,
    "view_orders": 102,
    "view_all_orders": 148
  }
}
```

### Control Endpoints

**POST** `/enable`
- Enables load generation
- Returns: `{"status": "enabled"}`

**POST** `/disable`
- Disables load generation
- Returns: `{"status": "disabled"}`

**POST** `/intensity?level={low|medium|high}`
- Sets load intensity
- Returns: `{"intensity": "high"}`

**POST** `/reset`
- Resets all statistics
- Returns: `{"status": "reset"}`

**GET** `/control`
- Lists available control commands

**GET** `/`
- Service information

## Configuration

Edit `load-generator/src/main/resources/application.properties`:

```properties
# Server Configuration
server.port=9090

# Gateway URL (where to send requests)
gateway.url=http://localhost:8080

# Load Configuration
load.enabled=true          # Start generating load immediately
load.users=20             # Number of simulated users
load.intensity=medium     # Load intensity level

# Logging
logging.level.com.demo.loadgen=INFO
```

## Architecture

```
LoadGeneratorApplication
    ↓
LoadGeneratorService (Scheduled Tasks)
    ↓
WebClient → API Gateway → Microservices
```

### Key Components

1. **LoadGeneratorService**
   - Main service with @Scheduled methods
   - Manages traffic generation
   - Tracks statistics

2. **ControlController**
   - REST API for controlling load
   - Exposes statistics
   - Runtime configuration

3. **WebClient**
   - Non-blocking HTTP client
   - Concurrent request handling
   - Timeout and error handling

## User Journey Details

### Quick Browser Journey

```
1. Browse all products
2. View random product
3. View another random product
4. Exit
```

**Duration:** ~6 seconds  
**Requests:** 3

### Window Shopper Journey

```
1. Browse all products
2. View specific product
3. Add product to cart
4. Add another product to cart
5. View cart
6. Exit (no checkout)
```

**Duration:** ~15 seconds  
**Requests:** 5

### Buyer Journey

```
1. Browse all products
2. View specific product
3. Add product to cart (3 items)
4. View cart
5. Proceed to checkout
6. View order confirmation
```

**Duration:** ~20 seconds  
**Requests:** 6

### Returning Customer Journey

```
1. View order history
2. Browse products
3. View specific product
4. Add to cart
5. View cart
```

**Duration:** ~15 seconds  
**Requests:** 5

### Product Explorer Journey

```
1. Browse all products
2. View product #1
3. View product #2
4. View product #3
5. View product #4
6. View product #5
```

**Duration:** ~18 seconds  
**Requests:** 6

## Statistics

The load generator tracks:

- **Total Requests**: All HTTP requests made
- **Successful Requests**: 2xx responses
- **Failed Requests**: Non-2xx responses or errors
- **Average Response Time**: Mean latency in ms
- **Success Rate**: Percentage of successful requests
- **Action Breakdown**: Count per action type

Statistics are:
- Printed to console every 30 seconds
- Available via REST API anytime
- Can be reset via API

## Customization

### Adding New User Journeys

Edit `LoadGeneratorService.java`:

```java
private Mono<String> myCustomJourney(String userId) {
    return browseProducts()
        .then(viewProduct(getRandomProductId()))
        .then(addToCart(userId, getRandomProductId(), 1))
        .then(checkout(userId, items))
        .then(Mono.just("Custom journey completed"));
}
```

Then add to journey selection:

```java
return switch (journeyType) {
    case 0 -> quickBrowseJourney(userId);
    case 1 -> browseAndAddToCart(userId);
    case 2 -> completeCheckoutJourney(userId);
    case 3 -> returningCustomerJourney(userId);
    case 4 -> myCustomJourney(userId);  // New journey
    default -> productExplorationJourney(userId);
};
```

### Changing Request Frequency

Modify `@Scheduled` annotations:

```java
// From every 2 seconds
@Scheduled(fixedDelay = 2000)

// To every 1 second (more aggressive)
@Scheduled(fixedDelay = 1000)

// Or every 5 seconds (less aggressive)
@Scheduled(fixedDelay = 5000)
```

### Adding New Actions

1. Create the API call method:
```java
private Mono<String> myNewAction(String param) {
    return makeRequest("my_action",
        webClient.get()
            .uri(gatewayUrl + "/api/my-endpoint")
            .retrieve()
            .bodyToMono(String.class)
    );
}
```

2. Add to action counts:
```java
actionCounts.put("my_action", new AtomicInteger(0));
```

## Monitoring the Load Generator

### Console Output

The load generator prints statistics every 30 seconds:

```
╔════════════════════════════════════════╗
║         Load Generator Stats           ║
╚════════════════════════════════════════╝
Total Requests: 1250
Successful: 1248 (99.84%)
Failed: 2
Avg Response Time: 45ms

Action Breakdown:
  browse_products: 245
  view_product: 312
  add_to_cart: 198
  view_cart: 156
  checkout: 89
  view_orders: 102
  view_all_orders: 148
════════════════════════════════════════
```

### Log File

View detailed logs:

```bash
tail -f logs/load-generator.log
```

### REST API

Real-time statistics:

```bash
# View stats
curl http://localhost:9090/stats | jq

# Watch stats (updates every 2 seconds)
watch -n 2 'curl -s http://localhost:9090/stats | jq'
```

## Performance Considerations

### Resource Usage

- **Memory**: ~200 MB
- **CPU**: 5-15% (depending on intensity)
- **Network**: Minimal (localhost only)

### Scalability

The load generator can handle:
- **Low**: 100 requests/min
- **Medium**: 300 requests/min
- **High**: 500+ requests/min

For higher load, consider:
- Running multiple load generator instances
- Using dedicated load testing tools (JMeter, Gatling)

## Troubleshooting

### Load Generator Not Generating Traffic

```bash
# Check if enabled
curl http://localhost:9090/stats

# Enable if needed
curl -X POST http://localhost:9090/enable
```

### High Error Rate

```bash
# Check if gateway is running
curl http://localhost:8080/api/health

# Check other services
curl http://localhost:8081/api/products
curl http://localhost:8082/api/cart/test
curl http://localhost:8083/api/orders
```

### Low Request Volume

```bash
# Increase intensity
curl -X POST 'http://localhost:9090/intensity?level=high'

# Verify configuration
cat load-generator/src/main/resources/application.properties
```

### Connection Timeouts

Edit `LoadGeneratorService.java`:

```java
.timeout(Duration.ofSeconds(10))  // Increase from 5 to 10
```

## Integration with APM Tools

The load generator works seamlessly with APM tools:

1. **Elastic APM**: Tracks all HTTP requests and transactions
2. **New Relic**: Monitors request rates and response times
3. **Datadog**: Visualizes traffic patterns
4. **Prometheus**: Exposes metrics (can be added)

See `APM_MONITORING.md` for detailed APM integration guide.

## Best Practices

1. **Warm-up Period**: Run for 2-3 minutes before demos
2. **Baseline First**: Start with medium intensity
3. **Gradual Increase**: Step up intensity for stress tests
4. **Monitor Resources**: Watch system resources
5. **Clean Statistics**: Reset before important demos

## Examples

### Demo Scenario: Normal Operations

```bash
# Start everything
./start-with-load.sh

# Wait 2 minutes for warm-up

# Show statistics
curl http://localhost:9090/stats | jq
```

### Demo Scenario: Load Spike

```bash
# Start with low load
curl -X POST 'http://localhost:9090/intensity?level=low'

# Show baseline metrics in APM

# Increase to high load
curl -X POST 'http://localhost:9090/intensity?level=high'

# Show impact in APM
```

### Demo Scenario: Load Testing

```bash
# Enable high load
curl -X POST 'http://localhost:9090/intensity?level=high'

# Run for 10 minutes
sleep 600

# Check statistics
curl http://localhost:9090/stats | jq

# Analyze in APM tool
```

## Stopping the Load Generator

```bash
# Disable load generation (keeps service running)
curl -X POST http://localhost:9090/disable

# Or stop completely
./stop-all.sh
```

---

For more information, see:
- `APM_MONITORING.md` - APM integration guide
- `README.md` - Main project documentation
- `ARCHITECTURE.md` - System architecture details


