#!/bin/bash

# Cart Service Error Diagnosis Script

echo "╔════════════════════════════════════════╗"
echo "║   Cart Service Error Diagnosis         ║"
echo "╚════════════════════════════════════════╝"
echo ""

echo "Checking Cart Service..."
echo ""

# Check if cart service is running
echo "1. Testing Cart Service availability:"
if curl -s http://localhost:8082/api/cart/test > /dev/null 2>&1; then
    echo "   ✓ Cart Service is responding"
else
    echo "   ✗ Cart Service not responding"
    echo "   Run: cd cart-service && mvn spring-boot:run"
fi
echo ""

# Check logs for errors
echo "2. Recent Cart Service errors:"
if [ -f logs/cart-service.log ]; then
    echo ""
    tail -50 logs/cart-service.log | grep -i "error\|exception\|caused by" | tail -10
    echo ""
else
    echo "   No log file found at logs/cart-service.log"
fi
echo ""

# Test API Gateway connection to Cart Service
echo "3. Testing API Gateway → Cart Service:"
RESPONSE=$(curl -s -w "\n%{http_code}" http://localhost:8080/api/cart/test 2>&1)
HTTP_CODE=$(echo "$RESPONSE" | tail -n 1)
if [ "$HTTP_CODE" = "200" ]; then
    echo "   ✓ API Gateway can reach Cart Service"
else
    echo "   ✗ API Gateway cannot reach Cart Service (HTTP $HTTP_CODE)"
fi
echo ""

# Test adding to cart directly
echo "4. Testing direct add to cart:"
RESULT=$(curl -s -X POST http://localhost:8082/api/cart/test-user/items \
  -H "Content-Type: application/json" \
  -d '{"productId":1,"productName":"Test","price":99.99,"quantity":1}' \
  -w "\n%{http_code}")

HTTP_CODE=$(echo "$RESULT" | tail -n 1)
if [ "$HTTP_CODE" = "200" ]; then
    echo "   ✓ Can add to cart directly"
else
    echo "   ✗ Cannot add to cart (HTTP $HTTP_CODE)"
    echo "   Response: $(echo "$RESULT" | head -n -1)"
fi
echo ""

echo "════════════════════════════════════════"
echo ""
echo "Next steps:"
echo ""
echo "1. View full cart service logs:"
echo "   tail -f logs/cart-service.log"
echo ""
echo "2. Check if H2 database is working:"
echo "   curl http://localhost:8082/h2-console"
echo ""
echo "3. Restart cart service:"
echo "   ./stop-all.sh"
echo "   ./start-with-ui.sh"
echo ""

