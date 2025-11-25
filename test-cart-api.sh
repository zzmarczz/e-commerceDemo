#!/bin/bash

# Test Cart API Directly
# This script tests if the cart API is working

echo "╔════════════════════════════════════════╗"
echo "║      Testing Cart API Endpoints        ║"
echo "╚════════════════════════════════════════╝"
echo ""

API_GATEWAY="http://localhost:8080"
CART_SERVICE="http://localhost:8082"

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo "Testing Cart Service directly..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Test 1: Get cart (should create if not exists)
echo "Test 1: GET cart for user 'testuser'"
RESPONSE=$(curl -s -w "\n%{http_code}" $CART_SERVICE/api/cart/testuser)
HTTP_CODE=$(echo "$RESPONSE" | tail -n 1)
BODY=$(echo "$RESPONSE" | head -n -1)

if [ "$HTTP_CODE" = "200" ]; then
    echo -e "${GREEN}✓ Success (HTTP $HTTP_CODE)${NC}"
    echo "Response: $BODY"
else
    echo -e "${RED}✗ Failed (HTTP $HTTP_CODE)${NC}"
    echo "Response: $BODY"
fi
echo ""

# Test 2: Add item to cart (direct to cart service)
echo "Test 2: POST add item to cart (direct)"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST $CART_SERVICE/api/cart/testuser/items \
  -H "Content-Type: application/json" \
  -d '{
    "productId": 1,
    "productName": "Test Product",
    "price": 99.99,
    "quantity": 1
  }')
HTTP_CODE=$(echo "$RESPONSE" | tail -n 1)
BODY=$(echo "$RESPONSE" | head -n -1)

if [ "$HTTP_CODE" = "200" ]; then
    echo -e "${GREEN}✓ Success (HTTP $HTTP_CODE)${NC}"
    echo "Response: $BODY"
else
    echo -e "${RED}✗ Failed (HTTP $HTTP_CODE)${NC}"
    echo "Response: $BODY"
    echo ""
    echo -e "${YELLOW}Check cart service logs:${NC}"
    tail -20 logs/cart-service.log
fi
echo ""

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "Testing via API Gateway..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Test 3: Get cart via gateway
echo "Test 3: GET cart via API Gateway"
RESPONSE=$(curl -s -w "\n%{http_code}" $API_GATEWAY/api/cart/testuser2)
HTTP_CODE=$(echo "$RESPONSE" | tail -n 1)
BODY=$(echo "$RESPONSE" | head -n -1)

if [ "$HTTP_CODE" = "200" ]; then
    echo -e "${GREEN}✓ Success (HTTP $HTTP_CODE)${NC}"
    echo "Response: $BODY"
else
    echo -e "${RED}✗ Failed (HTTP $HTTP_CODE)${NC}"
    echo "Response: $BODY"
fi
echo ""

# Test 4: Add item via gateway
echo "Test 4: POST add item via API Gateway"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST $API_GATEWAY/api/cart/testuser2/items \
  -H "Content-Type: application/json" \
  -d '{
    "productId": 2,
    "productName": "Gateway Test",
    "price": 49.99,
    "quantity": 2
  }')
HTTP_CODE=$(echo "$RESPONSE" | tail -n 1)
BODY=$(echo "$RESPONSE" | head -n -1)

if [ "$HTTP_CODE" = "200" ]; then
    echo -e "${GREEN}✓ Success (HTTP $HTTP_CODE)${NC}"
    echo "Response: $BODY"
else
    echo -e "${RED}✗ Failed (HTTP $HTTP_CODE)${NC}"
    echo "Response: $BODY"
    echo ""
    echo -e "${YELLOW}Check API gateway logs:${NC}"
    tail -20 logs/api-gateway.log
fi
echo ""

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "Summary:"
echo ""
echo "If tests fail, check:"
echo "1. Services running: ./check-services.sh"
echo "2. Cart service logs: tail -f logs/cart-service.log"
echo "3. API gateway logs: tail -f logs/api-gateway.log"
echo ""

