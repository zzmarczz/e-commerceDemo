#!/bin/bash

# Revenue Headers Debugging Script
# Run this on your EC2 instance to diagnose why APM doesn't see headers

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🔍 REVENUE HEADERS DEBUGGING SCRIPT"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Step 1: Check Git version
echo "📋 STEP 1: Checking deployed code version..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
git log --oneline -1
LATEST_COMMIT=$(git log --oneline -1 | grep -o "^[a-f0-9]*")
echo ""
echo "Latest commit: $LATEST_COMMIT"
echo ""
git log --oneline -5 | grep -E "(revenue|header|X-Order)"
echo ""

if git log --oneline -3 | grep -q "X- prefix"; then
    echo "✅ Code with X- prefix IS deployed"
else
    echo "❌ WARNING: Code with X- prefix NOT found in recent commits!"
    echo "   You need to: git pull, rebuild, restart"
fi
echo ""

# Step 2: Check if services are running
echo "📋 STEP 2: Checking if services are running..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Testing API Gateway (port 8080)..."
if curl -s http://localhost:8080/health > /dev/null 2>&1; then
    echo "✅ API Gateway is running"
else
    echo "❌ API Gateway is NOT running"
    echo "   Run: ./start-with-load.sh"
    exit 1
fi

echo "Testing Order Service (port 8083)..."
if curl -s http://localhost:8083/api/orders > /dev/null 2>&1; then
    echo "✅ Order Service is running"
else
    echo "❌ Order Service is NOT running"
    exit 1
fi
echo ""

# Step 3: Test checkout endpoint directly
echo "📋 STEP 3: Testing POST /api/orders/checkout endpoint..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Sending test order via API Gateway (port 8080)..."
echo ""

RESPONSE=$(curl -i -s http://localhost:8080/api/orders/checkout \
  -H "Content-Type: application/json" \
  -H "X-Session-ID: debug-test-$$" \
  -H "X-Journey-ID: debug-journey-$$" \
  -d '{
    "userId": "debug-user-'"$$"'",
    "items": [
      {
        "productId": 1,
        "productName": "DebugProduct",
        "price": 123.45,
        "quantity": 3
      }
    ]
  }' 2>&1)

echo "Response headers:"
echo "$RESPONSE" | grep -E "^(HTTP|X-Order|X-Item|X-Session|X-Journey|Content-Type)"
echo ""

# Check for revenue headers
HAS_ORDER_ID=$(echo "$RESPONSE" | grep -i "X-Order-Id:" | wc -l)
HAS_ORDER_VALUE=$(echo "$RESPONSE" | grep -i "X-Order-Value:" | wc -l)
HAS_ITEM_COUNT=$(echo "$RESPONSE" | grep -i "X-Item-Count:" | wc -l)

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📊 HEADER CHECK RESULTS:"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

if [ $HAS_ORDER_ID -gt 0 ]; then
    echo "✅ X-Order-Id header FOUND"
    ORDER_ID=$(echo "$RESPONSE" | grep -i "X-Order-Id:" | cut -d':' -f2 | tr -d ' \r')
    echo "   Value: $ORDER_ID"
else
    echo "❌ X-Order-Id header MISSING"
fi

if [ $HAS_ORDER_VALUE -gt 0 ]; then
    echo "✅ X-Order-Value header FOUND"
    ORDER_VALUE=$(echo "$RESPONSE" | grep -i "X-Order-Value:" | cut -d':' -f2 | tr -d ' \r')
    echo "   Value: \$$ORDER_VALUE (expected: \$370.35)"
else
    echo "❌ X-Order-Value header MISSING"
fi

if [ $HAS_ITEM_COUNT -gt 0 ]; then
    echo "✅ X-Item-Count header FOUND"
    ITEM_COUNT=$(echo "$RESPONSE" | grep -i "X-Item-Count:" | cut -d':' -f2 | tr -d ' \r')
    echo "   Value: $ITEM_COUNT (expected: 3)"
else
    echo "❌ X-Item-Count header MISSING"
fi
echo ""

# Step 4: Test Order Service directly
echo "📋 STEP 4: Testing Order Service directly (port 8083)..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Bypassing API Gateway to test Order Service..."
echo ""

DIRECT_RESPONSE=$(curl -i -s http://localhost:8083/api/orders/checkout \
  -H "Content-Type: application/json" \
  -H "X-Session-ID: direct-test-$$" \
  -H "X-Journey-ID: direct-journey-$$" \
  -d '{
    "userId": "direct-user-'"$$"'",
    "items": [
      {
        "productId": 2,
        "productName": "DirectProduct",
        "price": 99.99,
        "quantity": 1
      }
    ]
  }' 2>&1)

echo "Direct Order Service response headers:"
echo "$DIRECT_RESPONSE" | grep -E "^(HTTP|X-Order|X-Item)"
echo ""

DIRECT_HAS_HEADERS=$(echo "$DIRECT_RESPONSE" | grep -i "X-Order-Value:" | wc -l)

if [ $DIRECT_HAS_HEADERS -gt 0 ]; then
    echo "✅ Order Service IS adding headers"
else
    echo "❌ Order Service NOT adding headers"
    echo "   → Order Service needs to be rebuilt with latest code!"
fi
echo ""

# Step 5: Summary and recommendations
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📊 DIAGNOSIS SUMMARY:"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

if [ $HAS_ORDER_ID -gt 0 ] && [ $HAS_ORDER_VALUE -gt 0 ] && [ $HAS_ITEM_COUNT -gt 0 ]; then
    echo "✅✅✅ ALL REVENUE HEADERS ARE WORKING! ✅✅✅"
    echo ""
    echo "Headers are in the HTTP response."
    echo "If your APM still doesn't show them, the issue is APM configuration:"
    echo ""
    echo "Next steps:"
    echo "1. Wait 5-10 minutes for APM to collect new data"
    echo "2. Check APM for transactions AFTER this timestamp: $(date)"
    echo "3. Look for endpoint: POST /api/orders/checkout"
    echo "4. Check Response Headers section in transaction details"
    echo "5. If still not visible, configure APM to capture custom headers"
    echo "   → See APM_REVENUE_TRACKING_GUIDE.md for your APM tool"
    echo ""
    echo "Expected header names in APM:"
    echo "  - X-Order-Id or http.response.x_order_id"
    echo "  - X-Order-Value or http.response.x_order_value"
    echo "  - X-Item-Count or http.response.x_item_count"
    echo ""
    echo "(Field names vary by APM tool)"
    
elif [ $DIRECT_HAS_HEADERS -gt 0 ] && [ $HAS_ORDER_VALUE -eq 0 ]; then
    echo "⚠️  PARTIAL ISSUE: Gateway not forwarding headers"
    echo ""
    echo "Order Service IS adding headers, but Gateway is NOT forwarding them."
    echo ""
    echo "Fix:"
    echo "  cd ~/e-commerceDemo/api-gateway"
    echo "  mvn clean package -DskipTests"
    echo "  cd .."
    echo "  ./stop-all.sh && ./start-with-load.sh"
    
elif [ $DIRECT_HAS_HEADERS -eq 0 ]; then
    echo "❌ ORDER SERVICE NOT ADDING HEADERS"
    echo ""
    echo "Order Service is not adding revenue headers to responses."
    echo ""
    echo "Fix:"
    echo "  cd ~/e-commerceDemo"
    echo "  git pull  # Get latest code with X- prefix"
    echo "  cd order-service"
    echo "  mvn clean package -DskipTests"
    echo "  cd ../api-gateway"
    echo "  mvn clean package -DskipTests"
    echo "  cd .."
    echo "  ./stop-all.sh && ./start-with-load.sh"
    echo "  # Wait 60 seconds"
    echo "  ./debug-revenue-headers.sh  # Run this script again"
    
else
    echo "⚠️  UNKNOWN ISSUE"
    echo ""
    echo "Try:"
    echo "1. Check if services are using latest JAR files"
    echo "2. Rebuild everything: mvn clean package -DskipTests"
    echo "3. Restart: ./stop-all.sh && ./start-with-load.sh"
    echo "4. Run this script again"
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🔍 Debugging complete!"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

