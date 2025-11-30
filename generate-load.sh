#!/bin/bash

# Continuous Load Generator for E-Commerce Demo
# This script generates realistic traffic patterns for APM monitoring

echo "=========================================="
echo "E-Commerce APM Load Generator"
echo "=========================================="
echo ""

# Configuration
GATEWAY_URL="http://localhost:8080"
NUM_USERS=10
REQUESTS_PER_CYCLE=50
SLEEP_BETWEEN_REQUESTS=0.5
CONCURRENT_USERS=5

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Check if services are running
echo "Checking if API Gateway is running..."
if ! curl -s "$GATEWAY_URL/api/health" > /dev/null 2>&1; then
    echo -e "${RED}ERROR: API Gateway is not running!${NC}"
    echo "Please start the services first: ./start-all.sh"
    exit 1
fi

echo -e "${GREEN}✓ Services are running!${NC}"
echo ""
echo "Load Configuration:"
echo "  - Gateway URL: $GATEWAY_URL"
echo "  - Simulated Users: $NUM_USERS"
echo "  - Requests per cycle: $REQUESTS_PER_CYCLE"
echo "  - Concurrent Users: $CONCURRENT_USERS"
echo "  - Sleep between requests: ${SLEEP_BETWEEN_REQUESTS}s"
echo ""
echo -e "${YELLOW}Press Ctrl+C to stop${NC}"
echo ""

# Counter for statistics
TOTAL_REQUESTS=0
SUCCESSFUL_REQUESTS=0
FAILED_REQUESTS=0

# Function to generate random user ID
get_random_user() {
    echo "user$((RANDOM % NUM_USERS + 1))"
}

# Function to generate random product ID
get_random_product() {
    echo $((RANDOM % 5 + 1))
}

# Function to generate random quantity
get_random_quantity() {
    echo $((RANDOM % 3 + 1))
}

# Function to make request with error handling
make_request() {
    local method=$1
    local url=$2
    local data=$3
    local action=$4
    
    TOTAL_REQUESTS=$((TOTAL_REQUESTS + 1))
    
    if [ -z "$data" ]; then
        response=$(curl -s -w "\n%{http_code}" -X "$method" "$url" 2>&1)
    else
        response=$(curl -s -w "\n%{http_code}" -X "$method" "$url" \
            -H "Content-Type: application/json" \
            -d "$data" 2>&1)
    fi
    
    http_code=$(echo "$response" | tail -n 1)
    
    if [[ "$http_code" =~ ^2[0-9][0-9]$ ]]; then
        SUCCESSFUL_REQUESTS=$((SUCCESSFUL_REQUESTS + 1))
        echo -e "${GREEN}✓${NC} $action [${http_code}]"
    else
        FAILED_REQUESTS=$((FAILED_REQUESTS + 1))
        echo -e "${RED}✗${NC} $action [${http_code}]"
    fi
}

# Function to simulate a user browsing
browse_products() {
    local user=$1
    echo -e "${BLUE}[$user]${NC} Browsing products..."
    make_request "GET" "$GATEWAY_URL/api/products" "" "Browse all products"
    sleep $SLEEP_BETWEEN_REQUESTS
    
    # View specific product
    local product_id=$(get_random_product)
    make_request "GET" "$GATEWAY_URL/api/products/$product_id" "" "View product $product_id"
    sleep $SLEEP_BETWEEN_REQUESTS
}

# Function to simulate adding items to cart
add_to_cart() {
    local user=$1
    local product_id=$(get_random_product)
    local quantity=$(get_random_quantity)
    
    # Get product info first
    product_info=$(curl -s "$GATEWAY_URL/api/products/$product_id")
    product_name=$(echo "$product_info" | grep -o '"name":"[^"]*"' | cut -d'"' -f4)
    product_price=$(echo "$product_info" | grep -o '"price":[0-9.]*' | cut -d':' -f2)
    
    if [ -z "$product_name" ]; then
        product_name="Product$product_id"
        product_price="99.99"
    fi
    
    echo -e "${BLUE}[$user]${NC} Adding $quantity x $product_name to cart..."
    
    local cart_data="{\"productId\":$product_id,\"productName\":\"$product_name\",\"price\":$product_price,\"quantity\":$quantity}"
    make_request "POST" "$GATEWAY_URL/api/cart/$user/items" "$cart_data" "Add to cart"
    sleep $SLEEP_BETWEEN_REQUESTS
}

# Function to view cart
view_cart() {
    local user=$1
    echo -e "${BLUE}[$user]${NC} Viewing cart..."
    make_request "GET" "$GATEWAY_URL/api/cart/$user" "" "View cart"
    sleep $SLEEP_BETWEEN_REQUESTS
}

# Function to checkout
checkout() {
    local user=$1
    echo -e "${BLUE}[$user]${NC} Processing checkout..."
    
    # Get cart items
    cart=$(curl -s "$GATEWAY_URL/api/cart/$user")
    
    # Extract items from cart (simplified - just create checkout with random items)
    local product_id=$(get_random_product)
    local checkout_data="{\"userId\":\"$user\",\"items\":[{\"productId\":$product_id,\"productName\":\"Product$product_id\",\"price\":99.99,\"quantity\":1}]}"
    
    make_request "POST" "$GATEWAY_URL/api/orders/checkout" "$checkout_data" "Checkout"
    sleep $SLEEP_BETWEEN_REQUESTS
}

# Function to view orders
view_orders() {
    local user=$1
    echo -e "${BLUE}[$user]${NC} Viewing order history..."
    make_request "GET" "$GATEWAY_URL/api/orders/user/$user" "" "View orders"
    sleep $SLEEP_BETWEEN_REQUESTS
}

# Function to view all orders (admin action)
view_all_orders() {
    echo -e "${BLUE}[admin]${NC} Viewing all orders..."
    make_request "GET" "$GATEWAY_URL/api/orders" "" "View all orders"
    sleep $SLEEP_BETWEEN_REQUESTS
}

# Function to simulate a complete user journey
user_journey() {
    local user=$(get_random_user)
    local journey_type=$((RANDOM % 4))
    
    case $journey_type in
        0)
            # Browser: just looks around
            echo -e "${YELLOW}=== Browser Journey ($user) ===${NC}"
            browse_products "$user"
            browse_products "$user"
            ;;
        1)
            # Window Shopper: browses and adds to cart but doesn't checkout
            echo -e "${YELLOW}=== Window Shopper Journey ($user) ===${NC}"
            browse_products "$user"
            add_to_cart "$user"
            add_to_cart "$user"
            view_cart "$user"
            ;;
        2)
            # Buyer: complete purchase
            echo -e "${YELLOW}=== Buyer Journey ($user) ===${NC}"
            browse_products "$user"
            add_to_cart "$user"
            add_to_cart "$user"
            add_to_cart "$user"
            view_cart "$user"
            checkout "$user"
            view_orders "$user"
            ;;
        3)
            # Returning Customer: checks order history
            echo -e "${YELLOW}=== Returning Customer Journey ($user) ===${NC}"
            view_orders "$user"
            browse_products "$user"
            add_to_cart "$user"
            view_cart "$user"
            ;;
    esac
    
    echo ""
}

# Function to print statistics
print_stats() {
    local success_rate=0
    if [ $TOTAL_REQUESTS -gt 0 ]; then
        success_rate=$(awk "BEGIN {printf \"%.2f\", ($SUCCESSFUL_REQUESTS/$TOTAL_REQUESTS)*100}")
    fi
    
    echo ""
    echo "=========================================="
    echo "Statistics:"
    echo "  Total Requests: $TOTAL_REQUESTS"
    echo "  Successful: ${GREEN}$SUCCESSFUL_REQUESTS${NC}"
    echo "  Failed: ${RED}$FAILED_REQUESTS${NC}"
    echo "  Success Rate: ${success_rate}%"
    echo "=========================================="
    echo ""
}

# Trap Ctrl+C to show final statistics
trap 'echo ""; echo "Stopping load generator..."; print_stats; exit 0' INT

# Main load generation loop
cycle=1
while true; do
    echo -e "${YELLOW}╔════════════════════════════════════════╗${NC}"
    echo -e "${YELLOW}║  Cycle $cycle - $(date +"%H:%M:%S")  ${NC}"
    echo -e "${YELLOW}╚════════════════════════════════════════╝${NC}"
    echo ""
    
    # Simulate multiple concurrent user journeys
    for i in $(seq 1 $CONCURRENT_USERS); do
        user_journey &
    done
    
    # Wait for all background jobs to complete
    wait
    
    # Occasionally make admin requests
    if [ $((cycle % 5)) -eq 0 ]; then
        view_all_orders
    fi
    
    # Print stats every 10 cycles
    if [ $((cycle % 10)) -eq 0 ]; then
        print_stats
    fi
    
    cycle=$((cycle + 1))
    sleep 2
done


