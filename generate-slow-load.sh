#!/bin/bash

# Generate continuous load to showcase slow response times in APM

echo "╔═══════════════════════════════════════════════════════╗"
echo "║   APM DEMO: Continuous Load with Slow Orders          ║"
echo "╚═══════════════════════════════════════════════════════╝"
echo ""

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

API_GATEWAY_URL="http://localhost:8080"
DURATION=${1:-300}  # Default 5 minutes

echo -e "${BLUE}Generating load for ${DURATION} seconds...${NC}"
echo "This will create a mix of fast and slow requests"
echo "Press Ctrl+C to stop"
echo ""

START_TIME=$(date +%s)
REQUEST_COUNT=0
SLOW_COUNT=0
FAST_COUNT=0

# Function to handle cleanup on Ctrl+C
cleanup() {
    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo -e "${GREEN}Load generation stopped${NC}"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    ELAPSED=$(($(date +%s) - START_TIME))
    echo "Duration: ${ELAPSED}s"
    echo "Total requests: ${REQUEST_COUNT}"
    echo "  Fast requests (products): ${FAST_COUNT}"
    echo "  Slow requests (orders): ${SLOW_COUNT}"
    exit 0
}

trap cleanup SIGINT SIGTERM

while true; do
    CURRENT_TIME=$(date +%s)
    ELAPSED=$((CURRENT_TIME - START_TIME))
    
    if [ $ELAPSED -ge $DURATION ]; then
        cleanup
    fi
    
    REQUEST_COUNT=$((REQUEST_COUNT + 1))
    
    # 40% slow orders, 60% fast products
    if [ $((RANDOM % 10)) -lt 4 ]; then
        # Slow request
        echo -e "${YELLOW}[$(date '+%H:%M:%S')] 🐌 Request #${REQUEST_COUNT}: GET /api/orders (SLOW)${NC}"
        START=$(date +%s%N)
        curl -s "$API_GATEWAY_URL/api/orders" > /dev/null
        END=$(date +%s%N)
        DURATION_MS=$(( (END - START) / 1000000 ))
        echo -e "${RED}    ⏱️  Response time: ${DURATION_MS}ms${NC}"
        SLOW_COUNT=$((SLOW_COUNT + 1))
    else
        # Fast request
        echo -e "${GREEN}[$(date '+%H:%M:%S')] ⚡ Request #${REQUEST_COUNT}: GET /api/products (FAST)${NC}"
        curl -s "$API_GATEWAY_URL/api/products" > /dev/null
        FAST_COUNT=$((FAST_COUNT + 1))
    fi
    
    # Small delay between requests
    sleep 0.5
done


