#!/bin/bash

# Complete Cart Service Diagnosis and Fix

echo "╔════════════════════════════════════════╗"
echo "║  Cart Service - Full Diagnosis         ║"
echo "╚════════════════════════════════════════╝"
echo ""

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Step 1: Check git status
echo "1. Checking git status..."
GIT_COMMIT=$(git log --oneline -1)
echo "   Current commit: $GIT_COMMIT"

if echo "$GIT_COMMIT" | grep -q "713cba1"; then
    echo -e "   ${GREEN}✓ You have the cart fix${NC}"
else
    echo -e "   ${RED}✗ You don't have the cart fix yet${NC}"
    echo "   Run: git pull"
    echo ""
    exit 1
fi
echo ""

# Step 2: Check if cart service JAR is recent
echo "2. Checking cart service build..."
if [ -f "cart-service/target/cart-service-1.0.0.jar" ]; then
    JAR_TIME=$(stat -c %y cart-service/target/cart-service-1.0.0.jar 2>/dev/null || stat -f "%Sm" cart-service/target/cart-service-1.0.0.jar)
    echo "   JAR built: $JAR_TIME"
    
    # Check if JAR is older than 5 minutes
    if [ $(find cart-service/target/cart-service-1.0.0.jar -mmin +5 2>/dev/null | wc -l) -gt 0 ]; then
        echo -e "   ${YELLOW}⚠ JAR is old, needs rebuild${NC}"
        NEEDS_REBUILD=true
    else
        echo -e "   ${GREEN}✓ JAR is recent${NC}"
        NEEDS_REBUILD=false
    fi
else
    echo -e "   ${RED}✗ JAR not found, needs build${NC}"
    NEEDS_REBUILD=true
fi
echo ""

# Step 3: Check if services are running
echo "3. Checking services..."
if curl -s http://localhost:8082/api/cart/test > /dev/null 2>&1; then
    echo -e "   ${GREEN}✓ Cart service is running${NC}"
else
    echo -e "   ${RED}✗ Cart service not running${NC}"
    NEEDS_REBUILD=true
fi
echo ""

# Step 4: Check logs for specific errors
echo "4. Checking recent cart service errors..."
if [ -f "logs/cart-service.log" ]; then
    echo "   Last 20 error lines:"
    tail -100 logs/cart-service.log | grep -i "error\|exception\|caused" | tail -20
    echo ""
else
    echo -e "   ${YELLOW}⚠ No log file found${NC}"
fi
echo ""

# Step 5: Recommend action
echo "════════════════════════════════════════"
echo ""
if [ "$NEEDS_REBUILD" = true ]; then
    echo -e "${YELLOW}ACTION REQUIRED: Rebuild cart service${NC}"
    echo ""
    echo "Run these commands:"
    echo ""
    echo "  ./stop-all.sh"
    echo "  cd cart-service"
    echo "  mvn clean package -DskipTests"
    echo "  cd .."
    echo "  ./start-with-ui.sh"
    echo "  sleep 60"
    echo ""
else
    echo -e "${GREEN}Cart service looks good${NC}"
    echo ""
    echo "But you're still getting 500 error. Let's check the actual error:"
    echo ""
    echo "Run this and share the output:"
    echo ""
    echo "  tail -100 logs/cart-service.log | grep -A 10 \"ERROR\""
    echo ""
fi

