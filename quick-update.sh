#!/bin/bash

# Quick Update and Restart - For applying GitHub fixes

echo "╔════════════════════════════════════════╗"
echo "║     Quick Update from GitHub            ║"
echo "╚════════════════════════════════════════╝"
echo ""

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

# Step 1: Stop services
echo "Step 1: Stopping services..."
./stop-all.sh
sleep 2
echo ""

# Step 2: Pull latest code
echo "Step 2: Pulling latest code from GitHub..."
git pull
if [ $? -ne 0 ]; then
    echo -e "${RED}✗ Git pull failed${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Code updated${NC}"
echo ""

# Step 3: Rebuild changed services
echo "Step 3: Rebuilding services..."

# Rebuild api-gateway
echo "  - Rebuilding api-gateway..."
cd api-gateway
mvn clean package -DskipTests -q
if [ $? -ne 0 ]; then
    echo -e "${YELLOW}✗ api-gateway build failed${NC}"
    cd ..
    exit 1
fi
cd ..
echo -e "${GREEN}  ✓ api-gateway rebuilt${NC}"

# Rebuild cart-service
echo "  - Rebuilding cart-service..."
cd cart-service
mvn clean package -DskipTests -q
if [ $? -ne 0 ]; then
    echo -e "${YELLOW}✗ cart-service build failed${NC}"
    cd ..
    exit 1
fi
cd ..
echo -e "${GREEN}  ✓ cart-service rebuilt${NC}"
echo ""

# Step 4: Clean up database files
echo "Step 4: Cleaning database files..."
find . -name "*.db" -type f -delete 2>/dev/null
find . -name "cartdb.*" -type f -delete 2>/dev/null
echo -e "${GREEN}✓ Database cleaned${NC}"
echo ""

# Step 5: Restart
echo "Step 5: Starting all services..."
./start-with-ui.sh
echo ""

echo "════════════════════════════════════════"
echo -e "${GREEN}Update completed!${NC}"
echo "════════════════════════════════════════"
echo ""
echo "Wait 60 seconds, then test:"
echo ""
echo "  curl -X POST http://localhost:8080/api/cart/user800/items \\"
echo "    -H 'Content-Type: application/json' \\"
echo "    -d '{\"productId\":1,\"productName\":\"Laptop\",\"price\":999.99,\"quantity\":1}'"
echo ""
echo "Or open browser: http://3.10.224.202:3000"
echo ""


