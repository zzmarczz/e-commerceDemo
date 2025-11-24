# E-Commerce Demo Application - Architecture

## System Overview

This is a microservices-based e-commerce application where each service runs in its own JVM process. The services communicate via REST APIs.

```
┌─────────────────────────────────────────────────────────────┐
│                         Client                               │
│                    (curl, browser, etc.)                     │
└────────────────────────┬────────────────────────────────────┘
                         │
                         │ HTTP Requests
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                     API Gateway                              │
│                    Port: 8080                                │
│                    (Separate JVM #1)                         │
└─────────────┬───────────────┬───────────────┬───────────────┘
              │               │               │
              │               │               │
    ┌─────────▼─────────┐    │    ┌─────────▼──────────┐
    │  Product Service  │    │    │   Order Service    │
    │   Port: 8081      │    │    │    Port: 8083      │
    │ (Separate JVM #2) │    │    │  (Separate JVM #4) │
    │                   │    │    │                    │
    │  ┌──────────┐    │    │    │   ┌──────────┐    │
    │  │ H2 DB    │    │    │    │   │ H2 DB    │    │
    │  │(Products)│    │    │    │   │(Orders)  │    │
    │  └──────────┘    │    │    │   └──────────┘    │
    └──────────────────┘    │    └───────────────────┘
                            │
                   ┌────────▼────────┐
                   │  Cart Service   │
                   │   Port: 8082    │
                   │(Separate JVM #3)│
                   │                 │
                   │  ┌──────────┐  │
                   │  │ H2 DB    │  │
                   │  │(Carts)   │  │
                   │  └──────────┘  │
                   └─────────────────┘
```

## Service Details

### 1. API Gateway (JVM #1)
- **Port:** 8080
- **Purpose:** Single entry point for all client requests
- **Technology:** Spring Boot + WebFlux (for reactive HTTP client)
- **Responsibilities:**
  - Route requests to appropriate backend services
  - Unified API interface
  - Request forwarding

### 2. Product Service (JVM #2)
- **Port:** 8081
- **Purpose:** Manage product catalog
- **Database:** H2 in-memory (productdb)
- **Features:**
  - CRUD operations for products
  - Pre-loaded with sample products
  - Product inventory management

**Endpoints:**
- GET /api/products - List all products
- GET /api/products/{id} - Get product details
- POST /api/products - Create product
- PUT /api/products/{id} - Update product
- DELETE /api/products/{id} - Delete product

### 3. Cart Service (JVM #3)
- **Port:** 8082
- **Purpose:** Manage shopping carts
- **Database:** H2 in-memory (cartdb)
- **Features:**
  - Add/remove items from cart
  - Update item quantities
  - Cart persistence per user
  - Calculate cart totals

**Endpoints:**
- GET /api/cart/{userId} - Get user's cart
- POST /api/cart/{userId}/items - Add item to cart
- DELETE /api/cart/{userId}/items/{itemId} - Remove item
- DELETE /api/cart/{userId} - Clear cart

### 4. Order Service (JVM #4)
- **Port:** 8083
- **Purpose:** Process orders and checkout
- **Database:** H2 in-memory (orderdb)
- **Features:**
  - Checkout process
  - Order history
  - Order status tracking
  - Order management

**Endpoints:**
- POST /api/orders/checkout - Create order
- GET /api/orders/user/{userId} - Get user orders
- GET /api/orders/{orderId} - Get order details
- GET /api/orders - List all orders
- PUT /api/orders/{orderId}/status - Update order status

## Data Models

### Product
```java
{
  "id": Long,
  "name": String,
  "description": String,
  "price": Double,
  "stock": Integer
}
```

### Cart
```java
{
  "id": Long,
  "userId": String,
  "items": [CartItem],
  "total": Double
}
```

### CartItem
```java
{
  "id": Long,
  "productId": Long,
  "productName": String,
  "price": Double,
  "quantity": Integer
}
```

### Order
```java
{
  "id": Long,
  "userId": String,
  "orderDate": LocalDateTime,
  "totalAmount": Double,
  "status": OrderStatus,
  "items": [OrderItem]
}
```

### OrderStatus (Enum)
- PENDING
- CONFIRMED
- PROCESSING
- SHIPPED
- DELIVERED
- CANCELLED

## Communication Flow

### Example: Complete Purchase Flow

1. **Browse Products**
   ```
   Client → API Gateway → Product Service → H2 DB
                       ← Product List ←
   ```

2. **Add to Cart**
   ```
   Client → API Gateway → Cart Service → H2 DB
                       ← Updated Cart ←
   ```

3. **Checkout**
   ```
   Client → API Gateway → Order Service → H2 DB
                       ← Order Confirmation ←
   ```

## Technology Stack

| Component | Technology |
|-----------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 3.1.5 |
| Web | Spring Web MVC |
| HTTP Client | Spring WebFlux (Gateway) |
| Data Access | Spring Data JPA |
| Database | H2 (in-memory) |
| Build Tool | Maven |
| Architecture | Microservices |

## Deployment

Each service can be deployed independently:

### Development Mode
```bash
# Each service in separate terminal
cd product-service && mvn spring-boot:run
cd cart-service && mvn spring-boot:run
cd order-service && mvn spring-boot:run
cd api-gateway && mvn spring-boot:run
```

### Production-like Mode
```bash
# Build all services
mvn clean package

# Run JAR files (each in separate JVM)
java -jar product-service/target/product-service-1.0.0.jar &
java -jar cart-service/target/cart-service-1.0.0.jar &
java -jar order-service/target/order-service-1.0.0.jar &
java -jar api-gateway/target/api-gateway-1.0.0.jar &
```

### Quick Start Mode
```bash
./start-all.sh        # Starts all services with Maven
./start-all-jar.sh    # Starts all services with JAR files
./stop-all.sh         # Stops all services
```

## Key Features

✅ **Independent JVMs**: Each service runs in its own Java process  
✅ **Isolation**: Each service has its own database  
✅ **Scalability**: Services can be scaled independently  
✅ **Resilience**: Failure in one service doesn't crash others  
✅ **RESTful APIs**: Standard HTTP/JSON communication  
✅ **Easy Development**: Can run and test services independently  

## Extension Points

This architecture can be extended with:

- **Service Discovery** (Eureka, Consul)
- **Load Balancing** (Nginx, Spring Cloud LoadBalancer)
- **Circuit Breakers** (Resilience4j)
- **Distributed Tracing** (Zipkin, Jaeger)
- **API Authentication** (OAuth2, JWT)
- **Message Queue** (RabbitMQ, Kafka)
- **Centralized Config** (Spring Cloud Config)
- **Container Deployment** (Docker, Kubernetes)

## Monitoring & Debugging

### Check Service Health
```bash
curl http://localhost:8080/api/health  # Gateway
curl http://localhost:8081/api/products # Product Service
curl http://localhost:8082/api/cart/test # Cart Service
curl http://localhost:8083/api/orders # Order Service
```

### View Logs
```bash
tail -f logs/api-gateway.log
tail -f logs/product-service.log
tail -f logs/cart-service.log
tail -f logs/order-service.log
```

### Access H2 Console
- http://localhost:8081/h2-console (Product DB)
- http://localhost:8082/h2-console (Cart DB)
- http://localhost:8083/h2-console (Order DB)

## Performance Characteristics

- **Startup Time**: ~10-15 seconds per service
- **Memory Usage**: ~150-200 MB per service
- **Total Memory**: ~800 MB for all 4 services
- **Response Time**: <100ms for typical requests
- **Database**: In-memory (data lost on restart)

