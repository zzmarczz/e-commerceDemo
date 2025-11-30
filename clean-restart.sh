#!/bin/bash

# Complete Clean Restart - Fixes Duplicate Cart Issue

echo "╔════════════════════════════════════════╗"
echo "║   Clean Restart with Database Fix      ║"
echo "╚════════════════════════════════════════╝"
echo ""

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Step 1: Stop all services
echo "Step 1: Stopping all services..."
./stop-all.sh
sleep 2
echo ""

# Step 2: Kill any lingering Java processes (cleanup)
echo "Step 2: Cleaning up processes..."
pkill -f "cart-service" 2>/dev/null
pkill -f "spring-boot:run" 2>/dev/null
sleep 2
echo -e "${GREEN}✓ Processes cleaned${NC}"
echo ""

# Step 3: Remove any H2 database files
echo "Step 3: Removing any persisted database files..."
find . -name "*.db" -type f -delete 2>/dev/null
find . -name "cartdb.*" -type f -delete 2>/dev/null
rm -rf cart-service/*.db 2>/dev/null
echo -e "${GREEN}✓ Database files cleaned${NC}"
echo ""

# Step 4: Rebuild cart service
echo "Step 4: Rebuilding cart service with fixes..."
cd cart-service
mvn clean package -DskipTests -q
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Build successful${NC}"
else
    echo -e "${YELLOW}✗ Build failed - check errors above${NC}"
    exit 1
fi
cd ..
echo ""

# Step 5: Start all services
echo "Step 5: Starting all services..."
./start-with-ui.sh
echo ""

echo "════════════════════════════════════════"
echo -e "${GREEN}Clean restart completed!${NC}"
echo "════════════════════════════════════════"
echo ""
echo "Wait 60 seconds, then test:"
echo ""
echo "  curl -X POST http://localhost:8080/api/cart/user999/items \\"
echo "    -H 'Content-Type: application/json' \\"
echo "    -d '{\"productId\":1,\"productName\":\"Test\",\"price\":99.99,\"quantity\":1}'"
echo ""
echo "Or open browser: http://3.10.224.202:3000"
echo ""


