#!/bin/bash

# Network Access Configuration Script
# Configures all services to accept connections from any IP

echo "╔════════════════════════════════════════╗"
echo "║  Network Access Configuration          ║"
echo "╚════════════════════════════════════════╝"
echo ""

# Get IP address (auto-detect or use provided)
if [ -z "$1" ]; then
    # Try to auto-detect IP
    if [[ "$OSTYPE" == "darwin"* ]]; then
        # macOS
        IP_ADDRESS=$(ifconfig | grep "inet " | grep -v 127.0.0.1 | head -1 | awk '{print $2}')
    else
        # Linux
        IP_ADDRESS=$(hostname -I | awk '{print $1}')
    fi
    
    if [ -z "$IP_ADDRESS" ]; then
        echo "Could not auto-detect IP address."
        echo ""
        echo "Usage: ./configure-for-network.sh [IP_ADDRESS]"
        echo ""
        echo "Example: ./configure-for-network.sh 192.168.1.100"
        echo ""
        exit 1
    fi
    
    echo "Auto-detected IP: $IP_ADDRESS"
    echo ""
    read -p "Use this IP? (y/n): " -n 1 -r
    echo ""
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        read -p "Enter your IP address: " IP_ADDRESS
    fi
else
    IP_ADDRESS=$1
fi

echo ""
echo "Configuring for IP: $IP_ADDRESS"
echo ""

# Function to add server.address to properties file
add_bind_all() {
    local file=$1
    local service=$2
    
    if grep -q "server.address" "$file"; then
        sed -i.bak 's/server.address=.*/server.address=0.0.0.0/' "$file"
    else
        echo "server.address=0.0.0.0" >> "$file"
    fi
    echo "   ✓ $service configured to accept network connections"
}

# Configure each service to bind to all interfaces
echo "1. Configuring services to accept network connections..."
echo ""

add_bind_all "product-service/src/main/resources/application.properties" "Product Service"
add_bind_all "cart-service/src/main/resources/application.properties" "Cart Service"
add_bind_all "order-service/src/main/resources/application.properties" "Order Service"
add_bind_all "api-gateway/src/main/resources/application.properties" "API Gateway"
add_bind_all "frontend/src/main/resources/application.properties" "Frontend"

echo ""
echo "2. Updating CORS configuration..."

# Update CORS to allow all origins (more permissive for network access)
cat > "api-gateway/src/main/java/com/demo/gateway/config/CorsConfig.java" << 'EOF'
package com.demo.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        
        // Allow requests from any origin on the network
        config.addAllowedOriginPattern("http://*:3000");
        config.addAllowedOriginPattern("http://localhost:3000");
        
        // Allow all HTTP methods
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // Allow all headers
        config.setAllowedHeaders(Arrays.asList("*"));
        
        // Allow credentials
        config.setAllowCredentials(true);
        
        // Max age for preflight requests
        config.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
}
EOF

echo "   ✓ CORS configured to allow network access"
echo ""

# Update Frontend API URL
echo "3. Updating frontend API URL..."
JS_FILE="frontend/src/main/resources/static/js/app.js"

sed -i.bak "s|const API_BASE_URL = 'http://[^']*';|const API_BASE_URL = 'http://${IP_ADDRESS}:8080/api';|g" "$JS_FILE"

echo "   ✓ Frontend configured to use http://${IP_ADDRESS}:8080/api"
echo ""

# Update Gateway to use IP for backend services
echo "4. Updating API Gateway backend URLs..."
GATEWAY_PROPS="api-gateway/src/main/resources/application.properties"

sed -i.bak "s|services.product.url=http://[^:]*:|services.product.url=http://${IP_ADDRESS}:|g" "$GATEWAY_PROPS"
sed -i.bak "s|services.cart.url=http://[^:]*:|services.cart.url=http://${IP_ADDRESS}:|g" "$GATEWAY_PROPS"
sed -i.bak "s|services.order.url=http://[^:]*:|services.order.url=http://${IP_ADDRESS}:|g" "$GATEWAY_PROPS"

echo "   ✓ API Gateway configured"
echo ""

# Clean up backup files
find . -name "*.bak" -type f -delete

echo "════════════════════════════════════════"
echo "✓ Configuration Complete!"
echo "════════════════════════════════════════"
echo ""
echo "Your application is now configured for network access!"
echo ""
echo "Next steps:"
echo ""
echo "1. Rebuild the application:"
echo "   $ mvn clean install -DskipTests"
echo ""
echo "2. Stop any running services:"
echo "   $ ./stop-all.sh"
echo ""
echo "3. Start services:"
echo "   $ ./start-with-ui.sh"
echo ""
echo "4. Wait 60 seconds for startup"
echo ""
echo "5. Access from any device on your network:"
echo "   http://${IP_ADDRESS}:3000"
echo ""
echo "════════════════════════════════════════"
echo ""
echo "⚠️  Firewall Notice:"
echo ""
echo "Make sure your firewall allows incoming connections on:"
echo "  - Port 3000  (Frontend)"
echo "  - Port 8080  (API Gateway)"
echo "  - Ports 8081-8083 (Backend services)"
echo ""
echo "macOS: System Preferences → Security → Firewall"
echo "Linux: sudo ufw allow 3000,8080:8083/tcp"
echo ""
echo "════════════════════════════════════════"

