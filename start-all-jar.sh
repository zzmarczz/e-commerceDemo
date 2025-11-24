#!/bin/bash

# E-Commerce Demo Application Startup Script (JAR version)
# This script runs pre-built JAR files for faster startup

echo "Starting E-Commerce Demo Application (JAR mode)..."
echo "======================================="

# Check if JARs exist
if [ ! -f "product-service/target/product-service-1.0.0.jar" ]; then
    echo "JAR files not found. Building project..."
    mvn clean package -DskipTests
    if [ $? -ne 0 ]; then
        echo "Build failed. Please check the errors above."
        exit 1
    fi
fi

# Create logs directory if it doesn't exist
mkdir -p logs

echo "Starting services from JAR files..."

# Start Product Service
echo "Starting Product Service on port 8081..."
nohup java -jar product-service/target/product-service-1.0.0.jar > logs/product-service.log 2>&1 &
PRODUCT_PID=$!
echo "Product Service started (PID: $PRODUCT_PID)"

# Wait a bit for service to initialize
sleep 5

# Start Cart Service
echo "Starting Cart Service on port 8082..."
nohup java -jar cart-service/target/cart-service-1.0.0.jar > logs/cart-service.log 2>&1 &
CART_PID=$!
echo "Cart Service started (PID: $CART_PID)"

# Wait a bit for service to initialize
sleep 5

# Start Order Service
echo "Starting Order Service on port 8083..."
nohup java -jar order-service/target/order-service-1.0.0.jar > logs/order-service.log 2>&1 &
ORDER_PID=$!
echo "Order Service started (PID: $ORDER_PID)"

# Wait a bit for service to initialize
sleep 5

# Start API Gateway
echo "Starting API Gateway on port 8080..."
nohup java -jar api-gateway/target/api-gateway-1.0.0.jar > logs/api-gateway.log 2>&1 &
GATEWAY_PID=$!
echo "API Gateway started (PID: $GATEWAY_PID)"

# Save PIDs to file for stop script
echo $PRODUCT_PID > .pids
echo $CART_PID >> .pids
echo $ORDER_PID >> .pids
echo $GATEWAY_PID >> .pids

echo ""
echo "======================================="
echo "All services started successfully!"
echo "======================================="
echo ""
echo "Service URLs:"
echo "  - API Gateway:     http://localhost:8080"
echo "  - Product Service: http://localhost:8081"
echo "  - Cart Service:    http://localhost:8082"
echo "  - Order Service:   http://localhost:8083"
echo ""
echo "API Gateway Health: http://localhost:8080/api/health"
echo ""
echo "Logs are available in the 'logs' directory"
echo ""
echo "To stop all services, run: ./stop-all.sh"

