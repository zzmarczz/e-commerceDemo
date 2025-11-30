#!/bin/bash

# APM Demo: Simulate Slow Response Times in Orders Endpoint
# This script enables slow mode to showcase APM monitoring capabilities

echo "╔═══════════════════════════════════════════════════════╗"
echo "║   APM DEMO: Slow Response Time Simulator              ║"
echo "╚═══════════════════════════════════════════════════════╝"
echo ""

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

# Configuration
ORDER_SERVICE_URL="http://localhost:8083"
API_GATEWAY_URL="http://localhost:8080"

# Parse command line arguments
ACTION=${1:-"enable"}
DELAY=${2:-5000}

# Function to enable slow mode
enable_slow_mode() {
    echo -e "${YELLOW}🐌 ENABLING SLOW MODE...${NC}"
    echo "   Delay: ${DELAY}ms ($(echo "scale=1; $DELAY/1000" | bc)s)"
    echo ""
    
    RESPONSE=$(curl -s -X POST "$ORDER_SERVICE_URL/api/orders/control/slow-mode?enabled=true&delayMs=$DELAY")
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ Slow mode activated!${NC}"
        echo ""
        echo "$RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$RESPONSE"
        echo ""
        echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        echo -e "${BLUE}📊 APM DEMO READY${NC}"
        echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        echo ""
        echo "The /api/orders endpoint will now respond slowly."
        echo "Watch your APM dashboard for:"
        echo "  • Response time spikes"
        echo "  • Slow transaction detection"
        echo "  • Performance degradation alerts"
        echo ""
        echo -e "${YELLOW}Test it now:${NC}"
        echo ""
        echo "  # Normal endpoint (fast):"
        echo "  curl http://localhost:8080/api/products"
        echo ""
        echo "  # Slow endpoint (will take ${DELAY}ms):"
        echo "  curl http://localhost:8080/api/orders"
        echo ""
        echo "  # Or run continuous load:"
        echo "  ./generate-slow-load.sh"
        echo ""
        echo -e "${RED}To disable: ./demo-slow-orders.sh disable${NC}"
        echo ""
    else
        echo -e "${RED}✗ Failed to enable slow mode${NC}"
        echo "Make sure order-service is running on port 8083"
        exit 1
    fi
}

# Function to disable slow mode
disable_slow_mode() {
    echo -e "${GREEN}✅ DISABLING SLOW MODE...${NC}"
    echo ""
    
    RESPONSE=$(curl -s -X POST "$ORDER_SERVICE_URL/api/orders/control/slow-mode?enabled=false")
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ Slow mode deactivated!${NC}"
        echo ""
        echo "$RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$RESPONSE"
        echo ""
        echo "Normal response times restored."
        echo ""
    else
        echo -e "${RED}✗ Failed to disable slow mode${NC}"
        exit 1
    fi
}

# Function to check status
check_status() {
    echo -e "${BLUE}📊 CHECKING SLOW MODE STATUS...${NC}"
    echo ""
    
    RESPONSE=$(curl -s "$ORDER_SERVICE_URL/api/orders/control/slow-mode")
    
    if [ $? -eq 0 ]; then
        echo "$RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$RESPONSE"
        echo ""
    else
        echo -e "${RED}✗ Failed to check status${NC}"
        echo "Make sure order-service is running on port 8083"
        exit 1
    fi
}

# Main logic
case "$ACTION" in
    enable|on|start)
        enable_slow_mode
        ;;
    disable|off|stop)
        disable_slow_mode
        ;;
    status|check)
        check_status
        ;;
    test)
        echo "Testing slow endpoint..."
        echo ""
        echo "Starting request at: $(date '+%H:%M:%S')"
        START_TIME=$(date +%s)
        curl -s "$API_GATEWAY_URL/api/orders" > /dev/null
        END_TIME=$(date +%s)
        DURATION=$((END_TIME - START_TIME))
        echo "Finished request at: $(date '+%H:%M:%S')"
        echo ""
        echo "Response time: ${DURATION} seconds"
        ;;
    *)
        echo "Usage: $0 [enable|disable|status|test] [delay_ms]"
        echo ""
        echo "Examples:"
        echo "  $0 enable          # Enable with 5s delay (default)"
        echo "  $0 enable 10000    # Enable with 10s delay"
        echo "  $0 disable         # Disable slow mode"
        echo "  $0 status          # Check current status"
        echo "  $0 test            # Test response time"
        exit 1
        ;;
esac


