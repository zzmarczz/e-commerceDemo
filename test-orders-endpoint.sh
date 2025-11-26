#!/bin/bash

# Test Orders Endpoint - Diagnostic Tool

echo "╔═══════════════════════════════════════════════════════╗"
echo "║         Orders Endpoint Diagnostic Test               ║"
echo "╚═══════════════════════════════════════════════════════╝"
echo ""

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

API_GATEWAY_URL="http://localhost:8080"
ORDER_SERVICE_URL="http://localhost:8083"

echo -e "${BLUE}Testing Order Service Endpoints...${NC}"
echo ""

# Test 1: Direct Order Service - GET /api/orders
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo -e "${YELLOW}Test 1: Direct Order Service${NC}"
echo "URL: ${ORDER_SERVICE_URL}/api/orders"
echo ""
echo "Response:"
curl -s "${ORDER_SERVICE_URL}/api/orders" | python3 -m json.tool 2>&1 || curl -s "${ORDER_SERVICE_URL}/api/orders"
echo ""

# Test 2: Via API Gateway - GET /api/orders
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo -e "${YELLOW}Test 2: Via API Gateway${NC}"
echo "URL: ${API_GATEWAY_URL}/api/orders"
echo ""
echo "Response:"
curl -s "${API_GATEWAY_URL}/api/orders" | python3 -m json.tool 2>&1 || curl -s "${API_GATEWAY_URL}/api/orders"
echo ""

# Test 3: Check response type
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo -e "${YELLOW}Test 3: Response Headers${NC}"
echo "URL: ${API_GATEWAY_URL}/api/orders"
echo ""
curl -I -s "${API_GATEWAY_URL}/api/orders"
echo ""

# Test 4: Check if it's an array
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo -e "${YELLOW}Test 4: Validate JSON Structure${NC}"
echo ""
RESPONSE=$(curl -s "${API_GATEWAY_URL}/api/orders")
echo "Raw response: $RESPONSE"
echo ""

# Check if response is valid JSON
if echo "$RESPONSE" | python3 -c "import sys, json; json.load(sys.stdin)" 2>/dev/null; then
    echo -e "${GREEN}✓ Valid JSON${NC}"
    
    # Check if it's an array
    if echo "$RESPONSE" | python3 -c "import sys, json; data = json.load(sys.stdin); sys.exit(0 if isinstance(data, list) else 1)" 2>/dev/null; then
        echo -e "${GREEN}✓ Response is an array${NC}"
        COUNT=$(echo "$RESPONSE" | python3 -c "import sys, json; print(len(json.load(sys.stdin)))")
        echo -e "${GREEN}✓ Contains $COUNT orders${NC}"
    else
        echo -e "${RED}✗ Response is NOT an array${NC}"
        echo "Type:" 
        echo "$RESPONSE" | python3 -c "import sys, json; data = json.load(sys.stdin); print(type(data))"
    fi
else
    echo -e "${RED}✗ Invalid JSON${NC}"
fi
echo ""

# Test 5: User-specific orders (for comparison)
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo -e "${YELLOW}Test 5: User-Specific Orders (for comparison)${NC}"
echo "URL: ${API_GATEWAY_URL}/api/orders/user/user123"
echo ""
echo "Response:"
curl -s "${API_GATEWAY_URL}/api/orders/user/user123" | python3 -m json.tool 2>&1 || curl -s "${API_GATEWAY_URL}/api/orders/user/user123"
echo ""

# Test 6: Slow mode status
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo -e "${YELLOW}Test 6: Slow Mode Status${NC}"
echo ""
curl -s "${ORDER_SERVICE_URL}/api/orders/control/slow-mode" | python3 -m json.tool 2>&1
echo ""

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo -e "${GREEN}Diagnostic Complete${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

