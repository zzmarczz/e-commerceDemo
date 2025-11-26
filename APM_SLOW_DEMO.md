# APM Demo: Simulating Slow Response Times

## Overview

This demo showcases how Application Performance Monitoring (APM) tools detect and alert on slow response times. The Order Service has a built-in "slow mode" that adds configurable delays to the `/api/orders` endpoint.

## 🎯 What This Demonstrates

Your APM tool should detect and visualize:

- **Response Time Spikes**: Orders endpoint taking 5+ seconds vs normal <100ms
- **Slow Transaction Detection**: Individual slow transactions in the trace view
- **Service Comparison**: Orders service performing poorly vs other services
- **Performance Degradation**: Real-time alerts when thresholds are exceeded
- **Transaction Traces**: Detailed breakdown showing where time is spent

## 🚀 Quick Start

### Step 1: Enable Slow Mode

```bash
./demo-slow-orders.sh enable
```

**Options:**
```bash
# Default 5 second delay
./demo-slow-orders.sh enable

# Custom delay (10 seconds)
./demo-slow-orders.sh enable 10000

# Custom delay (3 seconds) 
./demo-slow-orders.sh enable 3000
```

### Step 2: Generate Load

```bash
./generate-slow-load.sh
```

This will generate continuous traffic mixing:
- **40% slow requests** to `/api/orders` (5s+ response time)
- **60% fast requests** to `/api/products` (<100ms response time)

### Step 3: Watch Your APM Dashboard

Open your APM tool and observe:

1. **Response Time Graph**: Should show spikes for order service
2. **Transaction List**: Slow transactions marked in red/orange
3. **Service Map**: Order service highlighted as slow
4. **Traces**: Individual traces showing 5s+ spent in getAllOrders()

### Step 4: Disable When Done

```bash
./demo-slow-orders.sh disable
```

---

## 📊 Manual Testing

### Test Individual Requests

**Fast endpoint:**
```bash
time curl http://localhost:8080/api/products
# Expected: <100ms
```

**Slow endpoint:**
```bash
time curl http://localhost:8080/api/orders
# Expected: ~5000ms (5 seconds)
```

### Check Status

```bash
./demo-slow-orders.sh status
```

**Output:**
```json
{
  "slowModeEnabled": true,
  "delayMs": 5000
}
```

---

## 🔧 Advanced Usage

### Different Delay Times

```bash
# Moderate delay (2 seconds)
./demo-slow-orders.sh enable 2000

# Severe delay (15 seconds)
./demo-slow-orders.sh enable 15000

# Extreme delay (30 seconds)
./demo-slow-orders.sh enable 30000
```

### Custom Load Generation

Generate load for specific duration:

```bash
# Generate load for 2 minutes
./generate-slow-load.sh 120

# Generate load for 10 minutes
./generate-slow-load.sh 600
```

### API Control

You can also control slow mode via API:

```bash
# Enable slow mode via API
curl -X POST "http://localhost:8083/api/orders/control/slow-mode?enabled=true&delayMs=5000"

# Disable slow mode via API
curl -X POST "http://localhost:8083/api/orders/control/slow-mode?enabled=false"

# Check status via API
curl http://localhost:8083/api/orders/control/slow-mode
```

---

## 🎬 Demo Script for Presentations

### Scenario: "Detecting Performance Issues"

1. **Baseline** (1 min):
   ```bash
   # Show normal performance
   ./generate-slow-load.sh 60
   ```
   Point out: "Everything looks healthy, sub-100ms response times"

2. **Introduce Problem** (immediate):
   ```bash
   ./demo-slow-orders.sh enable
   ```
   Say: "Uh oh, users are reporting slow checkout times"

3. **Generate Load** (3 min):
   ```bash
   ./generate-slow-load.sh 180
   ```
   Point out in APM:
   - "Notice the response time spike to 5+ seconds"
   - "APM automatically detected the slow transactions"
   - "We can see exactly which service is the bottleneck"
   - "The trace shows time spent in getAllOrders method"

4. **Resolution** (immediate):
   ```bash
   ./demo-slow-orders.sh disable
   ```
   Say: "After fixing the issue, response times return to normal"
   Point out: "APM confirms the fix - back to sub-100ms"

---

## 📈 Expected APM Metrics

### Normal Mode (Slow Mode Disabled)

| Endpoint | Avg Response Time | P95 | P99 |
|----------|------------------|-----|-----|
| /api/products | 20-50ms | 80ms | 100ms |
| /api/orders | 20-50ms | 80ms | 100ms |
| /api/cart/* | 30-60ms | 100ms | 150ms |

### Slow Mode Enabled (5s delay)

| Endpoint | Avg Response Time | P95 | P99 |
|----------|------------------|-----|-----|
| /api/products | 20-50ms | 80ms | 100ms |
| /api/orders | **5000-5050ms** | **5100ms** | **5200ms** |
| /api/cart/* | 30-60ms | 100ms | 150ms |

---

## 🛠️ Troubleshooting

### Slow mode not working?

1. **Check order service is running:**
   ```bash
   ./check-services.sh
   ```

2. **Verify slow mode is enabled:**
   ```bash
   ./demo-slow-orders.sh status
   ```

3. **Check order service logs:**
   ```bash
   tail -f logs/order-service.log | grep "SLOW MODE"
   ```
   Should show: `🐌 SLOW MODE: Delaying response by 5000ms`

### APM not showing slow transactions?

1. **Verify requests are going through:**
   ```bash
   tail -f logs/api-gateway.log
   ```

2. **Check APM agent is installed and configured**

3. **Ensure APM agent is instrumenting Spring Boot apps**

---

## 🎓 Learning Points

This demo teaches:

1. **Performance Monitoring**: How APM tracks response times across services
2. **Bottleneck Identification**: Quickly identify which service is slow
3. **Transaction Tracing**: See detailed breakdowns of slow requests
4. **Alerting**: Configure alerts when response times exceed thresholds
5. **Service Health**: Understand performance baselines and degradation

---

## 🔗 Related Documentation

- [APM_MONITORING.md](APM_MONITORING.md) - Full APM integration guide
- [LOAD_GENERATOR_README.md](LOAD_GENERATOR_README.md) - Background load generation
- [TROUBLESHOOTING.md](TROUBLESHOOTING.md) - Common issues and solutions

---

## 💡 Tips for APM Tool Integration

### Elastic APM
- Look for "slow transactions" in APM tab
- Check "Trace samples" for detailed views
- Set up alert rules for response time > 1000ms

### New Relic
- View "Transactions" page for slow queries
- Use "Transaction trace" for detailed analysis
- Create alert policies for Apdex score drops

### Datadog APM
- Check "Service Map" for red services
- View "Traces" tab for slow requests
- Set monitors for p95 latency thresholds

### AppDynamics
- Look at "Transaction Snapshots"
- Check "Slow, Very Slow, Stalled" categories
- Configure health rules for response time

---

**Questions or issues? Check the [TROUBLESHOOTING.md](TROUBLESHOOTING.md) guide.**

