#!/bin/bash

# Quick fix for cart error - rebuild and restart

echo "╔════════════════════════════════════════╗"
echo "║   Fixing Cart Service Error            ║"
echo "╚════════════════════════════════════════╝"
echo ""

echo "Step 1: Stopping all services..."
./stop-all.sh
echo ""

echo "Step 2: Rebuilding cart service..."
cd cart-service
mvn clean package -DskipTests
if [ $? -eq 0 ]; then
    echo "✓ Cart service rebuilt successfully"
else
    echo "✗ Build failed"
    exit 1
fi
cd ..
echo ""

echo "Step 3: Starting all services..."
./start-with-ui.sh
echo ""

echo "════════════════════════════════════════"
echo "Services are starting..."
echo "Wait 60 seconds, then test:"
echo ""
echo "  curl -X POST http://localhost:8082/api/cart/test/items \\"
echo "    -H 'Content-Type: application/json' \\"
echo "    -d '{\"productId\":1,\"productName\":\"Test\",\"price\":99.99,\"quantity\":1}'"
echo ""
echo "Or open browser: http://YOUR_IP:3000"
echo "════════════════════════════════════════"

