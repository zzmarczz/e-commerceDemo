#!/bin/bash

# E-Commerce Demo with Load Generator
# Starts all services + continuous load generator

echo "=============================================="
echo "E-Commerce Demo with Load Generator"
echo "=============================================="
echo ""

# Check if Maven is installed
if ! command -v mvn &> /dev/null
then
    echo "Maven is not installed. Please install Maven first."
    exit 1
fi

# Build all services including load generator
echo "Building all services (including load generator)..."
mvn clean install -DskipTests

if [ $? -ne 0 ]; then
    echo "Build failed. Please check the errors above."
    exit 1
fi

echo "Build successful!"
echo ""

# Create logs directory
mkdir -p logs

echo "Starting services..."
echo ""

# Start Product Service
echo "Starting Product Service on port 8081..."
cd product-service
nohup mvn spring-boot:run > ../logs/product-service.log 2>&1 &
PRODUCT_PID=$!
echo "✓ Product Service started (PID: $PRODUCT_PID)"
cd ..
sleep 5

# Start Cart Service
echo "Starting Cart Service on port 8082..."
cd cart-service
nohup mvn spring-boot:run > ../logs/cart-service.log 2>&1 &
CART_PID=$!
echo "✓ Cart Service started (PID: $CART_PID)"
cd ..
sleep 5

# Start Order Service
echo "Starting Order Service on port 8083..."
cd order-service
nohup mvn spring-boot:run > ../logs/order-service.log 2>&1 &
ORDER_PID=$!
echo "✓ Order Service started (PID: $ORDER_PID)"
cd ..
sleep 5

# Start API Gateway
echo "Starting API Gateway on port 8080..."
cd api-gateway
nohup mvn spring-boot:run > ../logs/api-gateway.log 2>&1 &
GATEWAY_PID=$!
echo "✓ API Gateway started (PID: $GATEWAY_PID)"
cd ..
sleep 5

# Start Load Generator
echo "Starting Load Generator on port 9090..."
cd load-generator
nohup mvn spring-boot:run > ../logs/load-generator.log 2>&1 &
LOADGEN_PID=$!
echo "✓ Load Generator started (PID: $LOADGEN_PID)"
cd ..
sleep 3

# Save PIDs to file for stop script
echo $PRODUCT_PID > .pids
echo $CART_PID >> .pids
echo $ORDER_PID >> .pids
echo $GATEWAY_PID >> .pids
echo $LOADGEN_PID >> .pids

echo ""
echo "=============================================="
echo "All services started successfully!"
echo "=============================================="
echo ""
echo "Service URLs:"
echo "  - API Gateway:      http://localhost:8080"
echo "  - Product Service:  http://localhost:8081"
echo "  - Cart Service:     http://localhost:8082"
echo "  - Order Service:    http://localhost:8083"
echo "  - Load Generator:   http://localhost:9090"
echo ""
echo "Load Generator Control:"
echo "  - Statistics:       http://localhost:9090/stats"
echo "  - Control Panel:    http://localhost:9090/control"
echo "  - Enable Load:      curl -X POST http://localhost:9090/enable"
echo "  - Disable Load:     curl -X POST http://localhost:9090/disable"
echo "  - Set Intensity:    curl -X POST 'http://localhost:9090/intensity?level=high'"
echo ""
echo "Logs are available in the 'logs' directory"
echo ""
echo "⚡ Load generation is ACTIVE - Perfect for APM monitoring!"
echo ""
echo "To stop all services: ./stop-all.sh"
echo "=============================================="

