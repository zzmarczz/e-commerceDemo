# APM Monitoring Guide

This guide explains how to use the E-Commerce Demo Application for showcasing APM (Application Performance Monitoring) tools.

## Overview

The application now includes a **Load Generator** that continuously generates realistic e-commerce traffic, making it perfect for demonstrating APM capabilities such as:

- 📊 Request throughput and latency
- 🔄 Distributed tracing across microservices
- 💾 Database query performance
- 🚨 Error rates and exceptions
- 📈 Service dependencies and topology
- 🎯 Business transaction monitoring

## Architecture for APM

```
┌─────────────────────────────────────────────────────────┐
│              Load Generator (Port 9090)                  │
│         Generates continuous traffic patterns            │
└────────────────────┬────────────────────────────────────┘
                     │
                     │ HTTP Requests (High Frequency)
                     │
          ┌──────────▼──────────┐
          │    API Gateway      │  ← APM Agent
          │    Port: 8080       │
          └──┬──────┬──────┬───┘
             │      │      │
    ┌────────▼──┐ ┌▼──────▼─────┐ ┌──────────▼─────┐
    │ Product   │ │   Cart      │ │    Order       │
    │ Service   │ │  Service    │ │   Service      │
    │ :8081     │ │   :8082     │ │    :8083       │
    │ ← APM     │ │  ← APM      │ │   ← APM        │
    └───────────┘ └─────────────┘ └────────────────┘
```

## Quick Start with Load Generation

### 1. Start All Services with Load Generator

```bash
./start-with-load.sh
```

This starts:
- All 4 microservices (Product, Cart, Order, Gateway)
- Load Generator that immediately begins generating traffic

### 2. Verify Load Generation

Check statistics:
```bash
curl http://localhost:9090/stats
```

Expected output:
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

## Load Generator Features

### Simulated User Journeys

The load generator simulates 5 types of realistic user journeys:

1. **Quick Browser** - Views products but doesn't interact
2. **Window Shopper** - Browses and adds to cart but doesn't checkout
3. **Buyer** - Complete purchase journey
4. **Returning Customer** - Checks order history and browses
5. **Product Explorer** - Deep dive into multiple products

### Traffic Patterns

- **High Frequency** (every 2s): Continuous user journeys
- **Medium Frequency** (every 5s): Cart operations
- **Low Frequency** (every 10s): Checkout operations
- **Admin Operations** (every 15s): Administrative queries

### Control the Load Generator

#### View Statistics
```bash
curl http://localhost:9090/stats
```

#### Enable/Disable Load
```bash
# Disable load generation
curl -X POST http://localhost:9090/disable

# Enable load generation
curl -X POST http://localhost:9090/enable
```

#### Adjust Load Intensity
```bash
# Low intensity (1 concurrent user)
curl -X POST 'http://localhost:9090/intensity?level=low'

# Medium intensity (3 concurrent users) - Default
curl -X POST 'http://localhost:9090/intensity?level=medium'

# High intensity (5 concurrent users)
curl -X POST 'http://localhost:9090/intensity?level=high'
```

#### Reset Statistics
```bash
curl -X POST http://localhost:9090/reset
```

## APM Tool Integration

### Common APM Tools

The application is compatible with popular APM tools:

1. **Elastic APM**
2. **New Relic**
3. **Datadog**
4. **Dynatrace**
5. **AppDynamics**
6. **Prometheus + Grafana**
7. **Jaeger (OpenTelemetry)**

### Integration Steps (Generic)

#### 1. Add APM Agent to Services

For Java-based APM agents, you typically add the agent JAR:

**Option A: Modify startup scripts**
```bash
# In start-with-load.sh, change from:
nohup mvn spring-boot:run > logs/product-service.log 2>&1 &

# To:
nohup java -javaagent:/path/to/apm-agent.jar \
  -jar product-service/target/product-service-1.0.0.jar \
  > logs/product-service.log 2>&1 &
```

**Option B: Add to pom.xml**
```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
            <configuration>
                <agents>
                    <agent>${path.to.apm.agent}</agent>
                </agents>
            </configuration>
        </plugin>
    </plugins>
</build>
```

#### 2. Configure APM Agent

Create `application.properties` or environment variables:
```properties
# Example for Elastic APM
elastic.apm.service_name=product-service
elastic.apm.server_urls=http://localhost:8200
elastic.apm.application_packages=com.demo

# Example for New Relic
newrelic.config.app_name=product-service
newrelic.config.license_key=YOUR_KEY
```

#### 3. Start Load Generation

Once APM is configured, start the application with load:
```bash
./start-with-load.sh
```

## What to Showcase with APM

### 1. Service Topology & Dependencies

The load generator hits all services, showing:
- API Gateway → Product Service
- API Gateway → Cart Service
- API Gateway → Order Service

**APM View**: Service map showing all dependencies

### 2. Transaction Traces

Each user journey creates distributed traces:
- Browse → View Product → Add to Cart → Checkout → View Orders

**APM View**: Distributed trace waterfall charts

### 3. Throughput & Latency

With continuous load, you'll see:
- Requests per minute: ~200-500 (depending on intensity)
- Average response time: 20-100ms
- P95/P99 latencies

**APM View**: Time-series graphs of throughput and latency

### 4. Error Monitoring

The load generator includes error handling:
- 404 errors when viewing non-existent orders
- Timeout scenarios
- Network failures

**APM View**: Error rate graphs and exception tracking

### 5. Database Monitoring

Each service has H2 database operations:
- SELECT queries (product browsing)
- INSERT queries (add to cart, create order)
- UPDATE queries (cart updates)

**APM View**: Database query performance metrics

### 6. Business Metrics

Track business transactions:
- Product views
- Cart additions
- Checkout completions
- Order value

**APM View**: Business transaction dashboards

### 7. Resource Utilization

Monitor JVM metrics across 5 JVMs:
- Heap memory usage
- GC activity
- Thread counts
- CPU usage

**APM View**: Infrastructure metrics

## Sample APM Scenarios

### Scenario 1: Normal Operations

```bash
# Start with medium load
./start-with-load.sh

# Let it run for 5 minutes
# Show: Stable performance, consistent latency
```

**What to demonstrate:**
- Consistent throughput
- Service health
- Response times
- Success rates

### Scenario 2: High Load Stress Test

```bash
# Increase to high intensity
curl -X POST 'http://localhost:9090/intensity?level=high'

# Show: How system handles increased load
```

**What to demonstrate:**
- Throughput increase
- Latency impact
- Resource utilization
- Potential bottlenecks

### Scenario 3: Service Recovery

```bash
# Stop one service (e.g., Cart Service)
kill <CART_SERVICE_PID>

# Observe errors in APM
# Restart service
cd cart-service && mvn spring-boot:run &

# Show: Service recovery
```

**What to demonstrate:**
- Error spike detection
- Service downtime
- Recovery time
- Circuit breaker patterns (if implemented)

### Scenario 4: Distributed Tracing

```bash
# Enable high load for complex traces
curl -X POST 'http://localhost:9090/intensity?level=high'

# Filter for checkout transactions
```

**What to demonstrate:**
- End-to-end transaction tracing
- Service call chains
- Slowest operations
- Bottleneck identification

## Metrics to Monitor

### Application Metrics

| Metric | Expected Value | What It Shows |
|--------|---------------|---------------|
| Requests/sec | 10-50 | Overall throughput |
| Avg Response Time | 20-100ms | Performance baseline |
| Error Rate | <1% | Application health |
| Success Rate | >99% | Reliability |

### Service-Specific Metrics

**Product Service**
- Most accessed service
- High read operations
- Product queries

**Cart Service**
- Medium access frequency
- Mix of read/write
- Session management

**Order Service**
- Lower frequency
- High-value transactions
- Order processing

**API Gateway**
- All traffic passes through
- Routing latency
- Request distribution

### JVM Metrics

- Heap memory: 100-300 MB per service
- GC pauses: <50ms
- Thread count: 20-50 per service
- CPU usage: 5-20% per service

## Load Generator Configuration

Edit `load-generator/src/main/resources/application.properties`:

```properties
# Gateway URL
gateway.url=http://localhost:8080

# Load Configuration
load.enabled=true        # Auto-start load generation
load.users=20           # Number of simulated users
load.intensity=medium   # low, medium, or high
```

### Custom Load Patterns

Edit `LoadGeneratorService.java` to customize:
- Request frequency
- User journey types
- Product selection
- Checkout patterns

## Troubleshooting

### Load Generator Not Starting

```bash
# Check logs
tail -f logs/load-generator.log

# Verify gateway is running
curl http://localhost:8080/api/health
```

### Low Request Volume

```bash
# Increase intensity
curl -X POST 'http://localhost:9090/intensity?level=high'

# Verify load is enabled
curl http://localhost:9090/stats
```

### High Error Rate

```bash
# Check if all services are running
curl http://localhost:8080/api/products
curl http://localhost:8082/api/cart/test
curl http://localhost:8083/api/orders
```

## Alternative Load Options

### Option 1: Shell Script Load Generator

For simpler load patterns:
```bash
./generate-load.sh
```

Features:
- Colored console output
- Real-time statistics
- Simpler to understand
- Easy to modify

### Option 2: Manual Testing

For demo purposes, run specific scenarios:
```bash
./test-ecommerce-flow.sh
```

## Best Practices for APM Demos

1. **Pre-warm the system**: Run load for 2-3 minutes before demo
2. **Start with baseline**: Show normal operations first
3. **Create scenarios**: Plan specific scenarios to demonstrate
4. **Use real metrics**: Let actual data drive the narrative
5. **Show recovery**: Demonstrate how APM helps identify and fix issues
6. **Focus on value**: Tie metrics to business outcomes

## Next Steps

1. ✅ Start services with load: `./start-with-load.sh`
2. ✅ Verify load generation: `curl http://localhost:9090/stats`
3. ✅ Configure your APM tool
4. ✅ Watch metrics flow in
5. ✅ Create demo scenarios
6. ✅ Showcase APM capabilities!

## Support

For issues or questions:
- Check service logs in `logs/` directory
- Verify all services are running on their ports
- Ensure load generator is enabled
- Review APM agent configuration

Happy monitoring! 📊🚀


