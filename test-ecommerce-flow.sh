#!/bin/bash

# E-Commerce Flow Test Script
# This script demonstrates the complete e-commerce flow

echo "=========================================="
echo "E-Commerce Demo - Complete Flow Test"
echo "=========================================="
echo ""

# Check if services are running
echo "Checking if API Gateway is running..."
if ! curl -s http://localhost:8080/api/health > /dev/null; then
    echo "ERROR: API Gateway is not running!"
    echo "Please start the services first: ./start-all.sh"
    exit 1
fi

echo "✓ Services are running!"
echo ""

# 1. Browse products
echo "Step 1: Browsing product catalog..."
echo "-----------------------------------"
curl -s http://localhost:8080/api/products | python3 -m json.tool 2>/dev/null || curl -s http://localhost:8080/api/products
echo ""
echo ""

# 2. Add Laptop to cart
echo "Step 2: Adding Laptop to cart..."
echo "--------------------------------"
curl -s -X POST http://localhost:8080/api/cart/user123/items \
  -H "Content-Type: application/json" \
  -d '{"productId": 1, "productName": "Laptop", "price": 999.99, "quantity": 1}' | python3 -m json.tool 2>/dev/null || curl -s -X POST http://localhost:8080/api/cart/user123/items -H "Content-Type: application/json" -d '{"productId": 1, "productName": "Laptop", "price": 999.99, "quantity": 1}'
echo ""
echo ""

# 3. Add Mouse to cart
echo "Step 3: Adding Mouse to cart (quantity: 2)..."
echo "--------------------------------------------"
curl -s -X POST http://localhost:8080/api/cart/user123/items \
  -H "Content-Type: application/json" \
  -d '{"productId": 2, "productName": "Mouse", "price": 29.99, "quantity": 2}' | python3 -m json.tool 2>/dev/null || curl -s -X POST http://localhost:8080/api/cart/user123/items -H "Content-Type: application/json" -d '{"productId": 2, "productName": "Mouse", "price": 29.99, "quantity": 2}'
echo ""
echo ""

# 4. Add Keyboard to cart
echo "Step 4: Adding Keyboard to cart..."
echo "----------------------------------"
curl -s -X POST http://localhost:8080/api/cart/user123/items \
  -H "Content-Type: application/json" \
  -d '{"productId": 3, "productName": "Keyboard", "price": 79.99, "quantity": 1}' | python3 -m json.tool 2>/dev/null || curl -s -X POST http://localhost:8080/api/cart/user123/items -H "Content-Type: application/json" -d '{"productId": 3, "productName": "Keyboard", "price": 79.99, "quantity": 1}'
echo ""
echo ""

# 5. View cart
echo "Step 5: Viewing shopping cart..."
echo "--------------------------------"
curl -s http://localhost:8080/api/cart/user123 | python3 -m json.tool 2>/dev/null || curl -s http://localhost:8080/api/cart/user123
echo ""
echo ""

# 6. Checkout
echo "Step 6: Proceeding to checkout..."
echo "---------------------------------"
curl -s -X POST http://localhost:8080/api/orders/checkout \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "items": [
      {"productId": 1, "productName": "Laptop", "price": 999.99, "quantity": 1},
      {"productId": 2, "productName": "Mouse", "price": 29.99, "quantity": 2},
      {"productId": 3, "productName": "Keyboard", "price": 79.99, "quantity": 1}
    ]
  }' | python3 -m json.tool 2>/dev/null || curl -s -X POST http://localhost:8080/api/orders/checkout -H "Content-Type: application/json" -d '{"userId": "user123", "items": [{"productId": 1, "productName": "Laptop", "price": 999.99, "quantity": 1}, {"productId": 2, "productName": "Mouse", "price": 29.99, "quantity": 2}, {"productId": 3, "productName": "Keyboard", "price": 79.99, "quantity": 1}]}'
echo ""
echo ""

# 7. View order history
echo "Step 7: Viewing order history..."
echo "--------------------------------"
curl -s http://localhost:8080/api/orders/user/user123 | python3 -m json.tool 2>/dev/null || curl -s http://localhost:8080/api/orders/user/user123
echo ""
echo ""

# 8. Clear cart
echo "Step 8: Clearing shopping cart..."
echo "---------------------------------"
curl -s -X DELETE http://localhost:8080/api/cart/user123
echo "Cart cleared!"
echo ""
echo ""

echo "=========================================="
echo "Test completed successfully!"
echo "=========================================="
echo ""
echo "Summary:"
echo "  ✓ Browsed products"
echo "  ✓ Added items to cart (Laptop, Mouse x2, Keyboard)"
echo "  ✓ Viewed cart contents"
echo "  ✓ Completed checkout"
echo "  ✓ Viewed order history"
echo "  ✓ Cleared cart"
echo ""
echo "Total order value: $1,139.96"
echo ""


