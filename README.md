# E-Commerce Demo Application

A microservices-based e-commerce demo application built with Java and Spring Boot. Each service runs in its own JVM, demonstrating a distributed system architecture.

**✨ NOW WITH WEB UI!** - Interactive browser-based interface included!

**GitHub Repository:** https://github.com/zzmarczz/e-commerceDemo.git

## Architecture

This application consists of 4 independent microservices:

1. **Product Service** (Port 8081) - Manages product catalog
2. **Cart Service** (Port 8082) - Handles shopping cart operations
3. **Order Service** (Port 8083) - Processes orders and checkout
4. **API Gateway** (Port 8080) - Unified entry point for all services

Each service runs in a separate JVM process and uses its own H2 in-memory database.

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- Port 8080-8083 should be available

To verify Java installation:
```bash
java -version
```

To verify Maven installation:
```bash
mvn -version
```

## Quick Start

### Standard Mode (Without Load Generation)

### Option 1: Using the Startup Script (Recommended)

1. Make the scripts executable:
```bash
chmod +x start-all.sh stop-all.sh start-all-jar.sh
```

2. Start all services:
```bash
./start-all.sh
```

This will:
- Build all services
- Start each service in the background
- Create log files in the `logs` directory

3. Stop all services:
```bash
./stop-all.sh
```

### Option 2: Using JAR files (Faster startup)

1. Build the project once:
```bash
mvn clean package -DskipTests
```

2. Start all services using JAR files:
```bash
./start-all-jar.sh
```

### Option 3: With UI + Load Generator (for APM monitoring)

Perfect for showcasing APM tools! Starts all services + UI + continuous load generation:

```bash
./start-with-load.sh
```

Then open:
- **Web UI:** http://localhost:3000
- **Load Stats:** http://localhost:9090/stats

This includes:
- Frontend Web UI (Port 3000)
- All 4 microservices
- Load Generator (Port 9090)
- Continuous realistic traffic

Control the load generator:
```bash
# View statistics
curl http://localhost:9090/stats

# Adjust intensity
curl -X POST 'http://localhost:9090/intensity?level=high'

# Enable/disable
curl -X POST http://localhost:9090/enable
curl -X POST http://localhost:9090/disable
```

See `APM_MONITORING.md` for complete APM setup guide.

### Option 4: Manual Start (for development)

Open 4 separate terminal windows and run:

**Terminal 1 - Product Service:**
```bash
cd product-service
mvn spring-boot:run
```

**Terminal 2 - Cart Service:**
```bash
cd cart-service
mvn spring-boot:run
```

**Terminal 3 - Order Service:**
```bash
cd order-service
mvn spring-boot:run
```

**Terminal 4 - API Gateway:**
```bash
cd api-gateway
mvn spring-boot:run
```

## Testing the Application

Once all services are running, you can test the application using curl or any HTTP client.

### 1. View Products

```bash
curl http://localhost:8080/api/products
```

### 2. Get a Specific Product

```bash
curl http://localhost:8080/api/products/1
```

### 3. Add Product to Cart

```bash
curl -X POST http://localhost:8080/api/cart/user123/items \
  -H "Content-Type: application/json" \
  -d '{
    "productId": 1,
    "productName": "Laptop",
    "price": 999.99,
    "quantity": 1
  }'
```

### 4. View Cart

```bash
curl http://localhost:8080/api/cart/user123
```

### 5. Checkout (Create Order)

```bash
curl -X POST http://localhost:8080/api/orders/checkout \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "items": [
      {
        "productId": 1,
        "productName": "Laptop",
        "price": 999.99,
        "quantity": 1
      },
      {
        "productId": 2,
        "productName": "Mouse",
        "price": 29.99,
        "quantity": 2
      }
    ]
  }'
```

### 6. View User Orders

```bash
curl http://localhost:8080/api/orders/user/user123
```

### 7. View All Orders

```bash
curl http://localhost:8080/api/orders
```

## Complete E-Commerce Flow Example

Here's a complete example of a typical e-commerce transaction:

```bash
# 1. Browse products
echo "1. Getting all products..."
curl http://localhost:8080/api/products
echo -e "\n"

# 2. Add items to cart
echo "2. Adding Laptop to cart..."
curl -X POST http://localhost:8080/api/cart/user123/items \
  -H "Content-Type: application/json" \
  -d '{"productId": 1, "productName": "Laptop", "price": 999.99, "quantity": 1}'
echo -e "\n"

echo "3. Adding Mouse to cart..."
curl -X POST http://localhost:8080/api/cart/user123/items \
  -H "Content-Type: application/json" \
  -d '{"productId": 2, "productName": "Mouse", "price": 29.99, "quantity": 2}'
echo -e "\n"

# 3. View cart
echo "4. Viewing cart..."
curl http://localhost:8080/api/cart/user123
echo -e "\n"

# 4. Checkout
echo "5. Checking out..."
curl -X POST http://localhost:8080/api/orders/checkout \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "items": [
      {"productId": 1, "productName": "Laptop", "price": 999.99, "quantity": 1},
      {"productId": 2, "productName": "Mouse", "price": 29.99, "quantity": 2}
    ]
  }'
echo -e "\n"

# 5. View order history
echo "6. Viewing order history..."
curl http://localhost:8080/api/orders/user/user123
echo -e "\n"
```

## API Endpoints

### Product Service (via Gateway: :8080/api/products)

- `GET /api/products` - Get all products
- `GET /api/products/{id}` - Get product by ID
- `POST /api/products` - Create new product
- `PUT /api/products/{id}` - Update product
- `DELETE /api/products/{id}` - Delete product

### Cart Service (via Gateway: :8080/api/cart)

- `GET /api/cart/{userId}` - Get user's cart
- `POST /api/cart/{userId}/items` - Add item to cart
- `DELETE /api/cart/{userId}/items/{itemId}` - Remove item from cart
- `DELETE /api/cart/{userId}` - Clear cart

### Order Service (via Gateway: :8080/api/orders)

- `POST /api/orders/checkout` - Create order from cart
- `GET /api/orders/user/{userId}` - Get user's orders
- `GET /api/orders/{orderId}` - Get specific order
- `GET /api/orders` - Get all orders
- `PUT /api/orders/{orderId}/status?status={STATUS}` - Update order status

### API Gateway

- `GET /api/health` - Health check

## Monitoring

### View Logs

All service logs are stored in the `logs` directory:
- `logs/product-service.log`
- `logs/cart-service.log`
- `logs/order-service.log`
- `logs/api-gateway.log`

Tail logs in real-time:
```bash
tail -f logs/api-gateway.log
```

### H2 Database Console

Each service has an H2 console for database inspection:
- Product Service: http://localhost:8081/h2-console
- Cart Service: http://localhost:8082/h2-console
- Order Service: http://localhost:8083/h2-console

Connection settings:
- JDBC URL: Check application.properties for each service
- Username: `sa`
- Password: (leave empty)

## Project Structure

```
DemoApp/
├── pom.xml                      # Parent POM
├── start-all.sh                # Startup script
├── start-all-jar.sh            # JAR-based startup script
├── start-with-load.sh          # Start with load generator
├── stop-all.sh                 # Stop script
├── generate-load.sh            # Shell-based load generator
├── test-ecommerce-flow.sh      # Demo test script
├── README.md                   # This file
├── APM_MONITORING.md           # APM setup guide
├── LOAD_GENERATOR_README.md    # Load generator docs
├── ARCHITECTURE.md             # Architecture details
├── QUICK_START.md              # Quick start guide
├── product-service/            # Product catalog service
│   ├── pom.xml
│   └── src/
├── cart-service/               # Shopping cart service
│   ├── pom.xml
│   └── src/
├── order-service/              # Order processing service
│   ├── pom.xml
│   └── src/
├── api-gateway/                # API Gateway
│   ├── pom.xml
│   └── src/
└── load-generator/             # Load generator service
    ├── pom.xml
    └── src/
```

## Technology Stack

- **Java 17** - Programming language
- **Spring Boot 3.1.5** - Application framework
- **Spring Data JPA** - Data persistence
- **H2 Database** - In-memory database
- **Maven** - Build tool
- **Spring WebFlux** - Reactive REST client (API Gateway)

## Features

✅ Microservices architecture with separate JVMs  
✅ RESTful APIs  
✅ Shopping cart management  
✅ Product catalog  
✅ Order processing and checkout  
✅ API Gateway for unified access  
✅ In-memory H2 databases (independent for each service)  
✅ Sample data initialization  
✅ Comprehensive logging  
✅ Easy startup/shutdown scripts  
✅ **Load Generator for APM monitoring**  
✅ **Continuous traffic generation**  
✅ **Realistic user journey simulation**  

## Troubleshooting

### Port Already in Use

If you get "port already in use" errors:

```bash
# Find and kill process on specific port (e.g., 8080)
lsof -ti:8080 | xargs kill -9
```

Or use the stop script:
```bash
./stop-all.sh
```

### Build Failures

Clean and rebuild:
```bash
mvn clean install -DskipTests
```

### Services Not Starting

Check the logs in the `logs` directory for detailed error messages.

## Development

### Building Individual Services

```bash
cd product-service
mvn clean package
```

### Running Individual Services

```bash
cd product-service
mvn spring-boot:run
```

### Running Tests

```bash
mvn test
```

## APM Monitoring & Load Testing

### Load Generator

The application includes a sophisticated load generator for APM monitoring:

```bash
# Start with load generation
./start-with-load.sh

# Or use shell-based load generator
./generate-load.sh
```

**Features:**
- Simulates 20 concurrent users
- 5 different user journey types
- 300+ requests per minute
- Realistic e-commerce behavior
- Real-time statistics

**Control API:**
- Statistics: `http://localhost:9090/stats`
- Control Panel: `http://localhost:9090/control`

**Documentation:**
- `APM_MONITORING.md` - Complete APM integration guide
- `LOAD_GENERATOR_README.md` - Load generator documentation
- `APM_SLOW_DEMO.md` - Slow response time simulation guide

### Simulating Slow Response Times (NEW!)

Showcase APM's ability to detect performance issues:

```bash
# Enable slow mode (5s delay on /api/orders)
./demo-slow-orders.sh enable

# Generate mixed traffic (fast + slow)
./generate-slow-load.sh

# Watch APM dashboard for:
#   - Response time spikes
#   - Slow transaction detection  
#   - Service bottleneck identification

# Disable slow mode
./demo-slow-orders.sh disable
```

**Perfect for demonstrating:**
- ⏱️ Response time monitoring
- 🔍 Slow transaction detection
- 🎯 Bottleneck identification
- 📊 Performance degradation alerts

See `APM_SLOW_DEMO.md` for complete guide.

### Use Cases for Load Generation

1. **APM Tool Demonstrations** - Showcase distributed tracing, metrics, slow transactions
2. **Performance Testing** - Test under realistic load
3. **Capacity Planning** - Understand system limits
4. **Training** - Learn microservices monitoring

## License

This is a demo application for educational purposes.

