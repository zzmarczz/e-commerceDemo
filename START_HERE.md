# 🚀 START HERE - E-Commerce Demo

## 🌐 For Interactive Use (Web UI)

### Quick Start with Browser Interface

```bash
./start-with-ui.sh
```

**Then open:** http://localhost:3000

You'll get a beautiful web interface where you can:
- ✨ Browse products
- 🛒 Add items to cart
- 💳 Complete checkout
- 📦 View orders
- 👤 Switch users

**Perfect for:** Demos, manual testing, presentations

---

## 📊 For APM Monitoring Demo

If you want to showcase an APM tool, follow these steps:

### 1. Start Everything with Load Generation

```bash
./start-with-load.sh
```

**What this does:**
- ✅ Starts all 4 microservices (Product, Cart, Order, Gateway)
- ✅ Starts Load Generator on port 9090
- ✅ Immediately begins generating realistic traffic
- ✅ Perfect for APM monitoring!

### 2. Wait 30-60 Seconds

Let the services start and data flow into your APM tool.

### 3. Check Load Statistics

```bash
curl http://localhost:9090/stats
```

You should see continuous traffic:
- Total Requests: increasing
- Success Rate: >99%
- Action Breakdown: all e-commerce operations

### 4. View Your APM Tool

Open your APM dashboard and you'll see:
- 📊 Continuous request throughput
- 🔄 Distributed traces across all services
- 📈 Latency metrics
- 🎯 Business transactions (browse, cart, checkout)

### 5. Control the Load

```bash
# Increase intensity for stress testing
curl -X POST 'http://localhost:9090/intensity?level=high'

# Decrease for baseline
curl -X POST 'http://localhost:9090/intensity?level=low'

# Pause
curl -X POST http://localhost:9090/disable

# Resume
curl -X POST http://localhost:9090/enable
```

### 6. Stop When Done

```bash
./stop-all.sh
```

---

## For Manual Testing

### Option 1: Web UI (Recommended)

```bash
./start-with-ui.sh
# Open http://localhost:3000
```

### Option 2: Command Line

```bash
# Start services
./start-all.sh

# Run manual test
./test-ecommerce-flow.sh
```

---

## What Gets Generated?

The load generator simulates:

1. **Browsers** - Users just looking around
2. **Window Shoppers** - Add to cart but don't buy
3. **Buyers** - Complete purchase journeys
4. **Returning Customers** - Check orders, then shop
5. **Product Explorers** - Deep product browsing

**Result:** 300-500 requests/minute with realistic patterns!

---

## Quick Links

- **Load Stats**: http://localhost:9090/stats
- **API Gateway**: http://localhost:8080
- **All Products**: http://localhost:8080/api/products

## Documentation

- `APM_MONITORING.md` - Complete APM setup guide
- `LOAD_GENERATOR_README.md` - Load generator documentation
- `README.md` - Full project documentation
- `QUICK_START.md` - Quick start without APM

---

## Troubleshooting

**Services not starting?**
```bash
# Check Java and Maven
java -version
mvn -version
```

**Load not generating?**
```bash
# Check if enabled
curl http://localhost:9090/stats

# Enable if needed
curl -X POST http://localhost:9090/enable
```

**Need help?**
Check logs in `logs/` directory or see README.md

---

🎉 **Ready for APM demo!** Start with `./start-with-load.sh` and watch the metrics flow!

