#!/bin/bash

# Service Health Check Script
# Checks if all services are running and accessible

echo "╔════════════════════════════════════════╗"
echo "║   E-Commerce Services Health Check     ║"
echo "╚════════════════════════════════════════╝"
echo ""

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to check service
check_service() {
    local name=$1
    local url=$2
    local port=$3
    
    printf "%-20s [Port %s] " "$name" "$port"
    
    if curl -s --max-time 2 "$url" > /dev/null 2>&1; then
        echo -e "${GREEN}✓ Running${NC}"
        return 0
    else
        echo -e "${RED}✗ Not responding${NC}"
        return 1
    fi
}

# Check each service
echo "Checking services..."
echo ""

check_service "Frontend" "http://localhost:3000" "3000"
check_service "API Gateway" "http://localhost:8080/api/health" "8080"
check_service "Product Service" "http://localhost:8081/api/products" "8081"
check_service "Cart Service" "http://localhost:8082/api/cart/test" "8082"
check_service "Order Service" "http://localhost:8083/api/orders" "8083"
check_service "Load Generator" "http://localhost:9090/stats" "9090"

echo ""
echo "════════════════════════════════════════"
echo ""

# Check for CORS issues
echo "Testing CORS from frontend to API Gateway..."
CORS_TEST=$(curl -s -X OPTIONS http://localhost:8080/api/products \
    -H "Origin: http://localhost:3000" \
    -H "Access-Control-Request-Method: GET" \
    -I | grep -i "access-control-allow-origin")

if [ ! -z "$CORS_TEST" ]; then
    echo -e "${GREEN}✓ CORS configured correctly${NC}"
else
    echo -e "${RED}✗ CORS may not be configured${NC}"
    echo "  Need to add CORS configuration to API Gateway"
fi

echo ""
echo "════════════════════════════════════════"
echo ""

# Check if any services are missing
if ! curl -s http://localhost:3000 > /dev/null 2>&1; then
    echo -e "${YELLOW}⚠ Frontend not running${NC}"
    echo "  Start with: ./start-with-ui.sh"
elif ! curl -s http://localhost:8080/api/health > /dev/null 2>&1; then
    echo -e "${YELLOW}⚠ API Gateway not running${NC}"
    echo "  This is required for the frontend to work"
    echo "  Start with: ./start-with-ui.sh"
else
    echo -e "${GREEN}✓ All core services are running${NC}"
    echo ""
    echo "Open your browser: http://localhost:3000"
fi

echo ""


