# Quick Start Guide

Get the e-commerce demo running in 5 minutes!

## Prerequisites Check

```bash
# Check Java (need 17+)
java -version

# Check Maven (need 3.6+)
mvn -version
```

If you need to install these, see the README.md for instructions.

## Start the Application

### Option 1: One-Command Start (Recommended for First Run)

```bash
./start-all.sh
```

This will:
- Build all services
- Start 4 separate JVMs
- Initialize sample data
- Create log files

Wait ~30 seconds for all services to start.

### Option 2: JAR Mode (Faster for Subsequent Runs)

```bash
./start-all-jar.sh
```

### Option 3: With Load Generator (Perfect for APM Demos!)

**Best for showcasing APM tools:**

```bash
./start-with-load.sh
```

This starts all services PLUS a load generator that continuously generates realistic traffic!

**Load Generator Features:**
- 🎭 Simulates 20 concurrent users
- 🔄 300+ requests/minute
- 📊 Real-time statistics at http://localhost:9090/stats
- 🎚️ Adjustable intensity (low/medium/high)

**Control the load:**
```bash
# View statistics
curl http://localhost:9090/stats

# Adjust intensity
curl -X POST 'http://localhost:9090/intensity?level=high'

# Pause load
curl -X POST http://localhost:9090/disable
```

See `APM_MONITORING.md` for complete APM setup!

## Verify It's Running

```bash
curl http://localhost:8080/api/health
# Should return: "API Gateway is running"

curl http://localhost:8080/api/products
# Should return: JSON array of products
```

Or open in browser:
- http://localhost:8080/api/products

## Run the Demo

```bash
./test-ecommerce-flow.sh
```

This will demonstrate:
1. Browse products
2. Add items to cart
3. View cart
4. Checkout
5. View orders

## Quick API Examples

### View Products
```bash
curl http://localhost:8080/api/products
```

### Add to Cart
```bash
curl -X POST http://localhost:8080/api/cart/myuser/items \
  -H "Content-Type: application/json" \
  -d '{
    "productId": 1,
    "productName": "Laptop",
    "price": 999.99,
    "quantity": 1
  }'
```

### View Cart
```bash
curl http://localhost:8080/api/cart/myuser
```

### Checkout
```bash
curl -X POST http://localhost:8080/api/orders/checkout \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "myuser",
    "items": [
      {"productId": 1, "productName": "Laptop", "price": 999.99, "quantity": 1}
    ]
  }'
```

### View Orders
```bash
curl http://localhost:8080/api/orders/user/myuser
```

## Stop the Application

```bash
./stop-all.sh
```

## Troubleshooting

### "Port already in use"
```bash
./stop-all.sh
# Or manually:
lsof -ti:8080 | xargs kill -9
```

### "Services not starting"
Check the logs:
```bash
tail -f logs/api-gateway.log
tail -f logs/product-service.log
```

### "Maven not found"
Install Maven:
```bash
# macOS
brew install maven

# Linux
sudo apt-get install maven
```

## What's Running?

After starting, you have 4 separate JVM processes:

| Service | Port | Purpose |
|---------|------|---------|
| API Gateway | 8080 | Entry point for all requests |
| Product Service | 8081 | Product catalog |
| Cart Service | 8082 | Shopping cart |
| Order Service | 8083 | Order processing |

## Next Steps

- Read `README.md` for complete documentation
- Read `ARCHITECTURE.md` for system design details
- Modify code in any service and restart just that service
- Add your own products, features, etc.

## Common Use Cases

### Add a New Product
```bash
curl -X POST http://localhost:8081/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Tablet",
    "description": "10-inch tablet",
    "price": 499.99,
    "stock": 20
  }'
```

### Update Order Status
```bash
curl -X PUT "http://localhost:8083/api/orders/1/status?status=SHIPPED"
```

### Clear a Cart
```bash
curl -X DELETE http://localhost:8082/api/cart/myuser
```

## Development Mode

Want to modify code? Start services individually:

```bash
# Terminal 1
cd product-service && mvn spring-boot:run

# Terminal 2
cd cart-service && mvn spring-boot:run

# Terminal 3
cd order-service && mvn spring-boot:run

# Terminal 4
cd api-gateway && mvn spring-boot:run
```

This way you can restart individual services as you make changes.

## Need Help?

- Check `README.md` - Complete documentation
- Check `ARCHITECTURE.md` - System architecture
- Check `logs/` directory - Service logs
- Visit H2 Console: http://localhost:8081/h2-console

Enjoy your e-commerce demo! 🛒

