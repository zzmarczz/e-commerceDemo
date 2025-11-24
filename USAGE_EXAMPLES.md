# Usage Examples

Practical examples for different use cases.

## 1. Quick APM Demo (5 minutes)

Perfect for showing an APM tool to someone quickly.

```bash
# Start everything with load
./start-with-load.sh

# Wait 30 seconds for warm-up
sleep 30

# Check that load is generating
curl http://localhost:9090/stats

# Open your APM tool dashboard
# - Show service topology
# - Show distributed traces
# - Show throughput graphs
# - Show latency metrics

# Increase load to show stress testing
curl -X POST 'http://localhost:9090/intensity?level=high'

# Wait 1 minute and show the impact
sleep 60

# Stop when done
./stop-all.sh
```

**Expected Results:**
- 300+ requests/minute at medium load
- 500+ requests/minute at high load
- Success rate >99%
- Traces across all 4 services

---

## 2. Long-Running Demo (30+ minutes)

For extended demos or training sessions.

```bash
# Start with medium load
./start-with-load.sh

# Let it run and showcase various APM features:

# After 5 minutes - Show baseline performance
curl http://localhost:9090/stats

# After 10 minutes - Increase load
curl -X POST 'http://localhost:9090/intensity?level=high'

# After 15 minutes - Show high load impact

# After 20 minutes - Return to normal
curl -X POST 'http://localhost:9090/intensity?level=medium'

# After 25 minutes - Simulate service failure
# (Stop one service to show error handling)
kill <PID_OF_SERVICE>

# After 27 minutes - Restart service
cd product-service && mvn spring-boot:run &

# After 30 minutes - Show recovery

# Clean up
./stop-all.sh
```

---

## 3. Before/After Comparison

Show the difference with and without load.

```bash
# Phase 1: No load
./start-all.sh

# Open APM tool - show quiet system
sleep 60

# Phase 2: With load
# Stop services
./stop-all.sh

# Start with load generator
./start-with-load.sh

# Open APM tool - show active system
sleep 60

# Comparison points:
# - Request volume
# - Service interactions
# - Trace availability
# - Metric density
```

---

## 4. Load Intensity Comparison

Demonstrate how system behaves under different loads.

```bash
# Start with low load
./start-with-load.sh
curl -X POST 'http://localhost:9090/intensity?level=low'

echo "Low load - ~100 req/min"
sleep 120
curl http://localhost:9090/stats

# Switch to medium load
curl -X POST 'http://localhost:9090/intensity?level=medium'
curl -X POST http://localhost:9090/reset

echo "Medium load - ~300 req/min"
sleep 120
curl http://localhost:9090/stats

# Switch to high load
curl -X POST 'http://localhost:9090/intensity?level=high'
curl -X POST http://localhost:9090/reset

echo "High load - ~500 req/min"
sleep 120
curl http://localhost:9090/stats

# Compare:
# - Request counts
# - Response times
# - Error rates
# - Resource utilization
```

---

## 5. Specific User Journey Focus

Focus on a particular user journey type.

### Browser Journey (View Only)

```bash
# Start services
./start-with-load.sh

# The load generator automatically includes browsers
# Filter your APM tool for:
# - GET /api/products
# - GET /api/products/{id}
# - No POST requests

# These are the "Quick Browser" journeys
```

### Checkout Journey (Complete Flow)

```bash
# Start services
./start-with-load.sh

# In APM tool, filter for checkout traces:
# 1. Browse products
# 2. View specific product
# 3. Add to cart (multiple items)
# 4. View cart
# 5. POST /api/orders/checkout
# 6. View order confirmation

# These are the "Buyer" journeys
# Show the complete distributed trace
```

---

## 6. Error Rate Monitoring

Demonstrate error detection and alerting.

```bash
# Start normally
./start-with-load.sh

# Observe normal error rate (<1%)
watch -n 5 'curl -s http://localhost:9090/stats | grep -A1 failed'

# Introduce errors by stopping a service
kill <CART_SERVICE_PID>

# Watch error rate increase
# APM tool should show:
# - Error spike
# - Failed service
# - Error traces

# Restart service
cd cart-service && mvn spring-boot:run &

# Watch error rate return to normal
```

---

## 7. Performance Baseline

Establish a performance baseline.

```bash
# Start with medium load
./start-with-load.sh

# Run for 10 minutes
echo "Collecting baseline metrics..."
sleep 600

# Capture statistics
curl http://localhost:9090/stats | tee baseline-stats.json

# In APM tool, note:
# - Average response time
# - P95/P99 latency
# - Request rate
# - Error rate
# - Resource utilization

# This is your baseline for comparison
```

---

## 8. Business Transaction Monitoring

Focus on business metrics.

```bash
# Start with load
./start-with-load.sh

# Track business transactions in APM:

# 1. Product Views
#    Endpoint: GET /api/products/{id}
#    Expected: ~100/min

# 2. Add to Cart
#    Endpoint: POST /api/cart/{userId}/items
#    Expected: ~65/min

# 3. Checkouts
#    Endpoint: POST /api/orders/checkout
#    Expected: ~30/min

# Monitor these as business KPIs
# Calculate conversion rates:
# - View to Cart: ~65%
# - Cart to Checkout: ~46%
```

---

## 9. Multi-Service Dependency Tracking

Show service dependencies.

```bash
# Start all services
./start-with-load.sh

# In APM tool, build service map showing:

# API Gateway depends on:
# - Product Service (high frequency)
# - Cart Service (medium frequency)
# - Order Service (low frequency)

# Each service depends on:
# - H2 Database (all services)

# Load generator depends on:
# - API Gateway (all requests)

# Visual: Service topology diagram
```

---

## 10. Resource Utilization Monitoring

Track resource consumption.

```bash
# Start with low load
./start-with-load.sh
curl -X POST 'http://localhost:9090/intensity?level=low'

# Monitor in APM:
# - CPU usage per service
# - Memory (heap) per service
# - GC activity
# - Thread counts

# Increase to high load
curl -X POST 'http://localhost:9090/intensity?level=high'

# Show resource increase
# - CPU should increase
# - Memory might increase
# - More GC activity
# - More active threads
```

---

## 11. Distributed Trace Deep Dive

Analyze a specific trace.

```bash
# Start services
./start-with-load.sh

# In APM tool:
# 1. Filter for "checkout" transactions
# 2. Select a trace
# 3. Analyze the waterfall:

# Expected trace structure:
# └─ API Gateway
#    ├─ Product Service (fetch product details)
#    ├─ Cart Service (validate cart)
#    └─ Order Service (create order)
#       └─ H2 Database (insert order)

# Show:
# - Time spent in each service
# - Network latency
# - Database query time
# - Total end-to-end time
```

---

## 12. Real-Time Statistics Dashboard

Monitor load generator statistics.

```bash
# Start services
./start-with-load.sh

# Watch statistics in real-time
watch -n 2 'curl -s http://localhost:9090/stats | jq'

# Or create a simple dashboard
while true; do
  clear
  echo "╔════════════════════════════════════════╗"
  echo "║     Load Generator Dashboard           ║"
  echo "╚════════════════════════════════════════╝"
  echo ""
  curl -s http://localhost:9090/stats | jq
  echo ""
  echo "Press Ctrl+C to stop"
  sleep 3
done
```

---

## 13. Custom Load Pattern

Create your own load pattern.

```bash
# Start services (load disabled)
./start-with-load.sh
curl -X POST http://localhost:9090/disable

# Define custom pattern
for i in {1..10}; do
  echo "Burst $i"
  
  # Enable load
  curl -X POST http://localhost:9090/enable
  curl -X POST 'http://localhost:9090/intensity?level=high'
  
  # Run for 30 seconds
  sleep 30
  
  # Disable load (quiet period)
  curl -X POST http://localhost:9090/disable
  
  # Wait 30 seconds
  sleep 30
done

# This creates a "burst" pattern
# - 30 seconds of high load
# - 30 seconds of no load
# - Repeat 10 times
```

---

## 14. Overnight Soak Test

Run for extended period.

```bash
# Start with low/medium load
./start-with-load.sh
curl -X POST 'http://localhost:9090/intensity?level=low'

# Run overnight
# (8 hours = 28800 seconds)
echo "Starting overnight test..."
echo "Stop with: ./stop-all.sh"

# Check in the morning
# Expected results after 8 hours:
# - Total requests: ~48,000
# - Success rate: >99%
# - No memory leaks
# - No service crashes
# - Stable response times
```

---

## 15. Training Exercise

Interactive learning session.

```bash
# Exercise 1: Start the system
./start-with-load.sh

# Exercise 2: Verify it's working
curl http://localhost:9090/stats

# Exercise 3: View API Gateway health
curl http://localhost:8080/api/health

# Exercise 4: Browse products
curl http://localhost:8080/api/products | jq

# Exercise 5: Add to cart
curl -X POST http://localhost:8080/api/cart/student1/items \
  -H "Content-Type: application/json" \
  -d '{"productId":1,"productName":"Laptop","price":999.99,"quantity":1}'

# Exercise 6: View cart
curl http://localhost:8080/api/cart/student1 | jq

# Exercise 7: Increase load
curl -X POST 'http://localhost:9090/intensity?level=high'

# Exercise 8: Monitor in APM tool
# - Find your manual requests
# - See them alongside generated load
# - Identify the distributed traces

# Exercise 9: Stop the system
./stop-all.sh
```

---

## Tips for Best Results

1. **Warm-up Period**: Always wait 1-2 minutes after starting
2. **Reset Stats**: Use `/reset` endpoint before important demos
3. **Save Baselines**: Capture baseline metrics for comparison
4. **Document Findings**: Take screenshots of APM dashboards
5. **Test Before Demo**: Always do a dry run
6. **Clean Environment**: Start fresh for each major demo

## Common Issues and Solutions

**Issue**: Low request volume  
**Solution**: `curl -X POST 'http://localhost:9090/intensity?level=high'`

**Issue**: High error rate  
**Solution**: Check if all services are running

**Issue**: No data in APM  
**Solution**: Verify APM agent is configured and running

**Issue**: Services not starting  
**Solution**: Check logs in `logs/` directory

---

Happy monitoring! 📊

