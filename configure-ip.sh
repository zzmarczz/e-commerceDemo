#!/bin/bash

# IP Configuration Script
# Configures the application to work with a custom IP address

echo "╔════════════════════════════════════════╗"
echo "║   E-Commerce IP Configuration          ║"
echo "╚════════════════════════════════════════╝"
echo ""

# Get the IP address
if [ -z "$1" ]; then
    echo "Usage: ./configure-ip.sh <your-ip-address>"
    echo ""
    echo "Example: ./configure-ip.sh 192.168.1.100"
    echo ""
    echo "This will configure:"
    echo "  - CORS in API Gateway to allow your IP"
    echo "  - Frontend to connect to API at your IP"
    echo ""
    exit 1
fi

IP_ADDRESS=$1

echo "Configuring for IP: $IP_ADDRESS"
echo ""

# Update CORS Configuration
echo "1. Updating CORS configuration..."
CORS_FILE="api-gateway/src/main/java/com/demo/gateway/config/CorsConfig.java"

cat > "$CORS_FILE" << EOF
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
        
        // Allow requests from frontend
        config.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",
            "http://127.0.0.1:3000",
            "http://${IP_ADDRESS}:3000"
        ));
        
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

echo "   ✓ CORS configuration updated"

# Update Frontend API URL
echo "2. Updating frontend API URL..."
JS_FILE="frontend/src/main/resources/static/js/app.js"

sed -i.bak "s|const API_BASE_URL = 'http://localhost:8080/api';|const API_BASE_URL = 'http://${IP_ADDRESS}:8080/api';|g" "$JS_FILE"

echo "   ✓ Frontend API URL updated"

# Update API Gateway service URLs (if services are on same IP)
echo "3. Updating API Gateway service URLs..."
GATEWAY_PROPS="api-gateway/src/main/resources/application.properties"

sed -i.bak "s|services.product.url=http://localhost:8081|services.product.url=http://${IP_ADDRESS}:8081|g" "$GATEWAY_PROPS"
sed -i.bak "s|services.cart.url=http://localhost:8082|services.cart.url=http://${IP_ADDRESS}:8082|g" "$GATEWAY_PROPS"
sed -i.bak "s|services.order.url=http://localhost:8083|services.order.url=http://${IP_ADDRESS}:8083|g" "$GATEWAY_PROPS"

echo "   ✓ API Gateway service URLs updated"

echo ""
echo "════════════════════════════════════════"
echo "Configuration Complete!"
echo "════════════════════════════════════════"
echo ""
echo "Next steps:"
echo ""
echo "1. Rebuild the application:"
echo "   mvn clean install -DskipTests"
echo ""
echo "2. Stop any running services:"
echo "   ./stop-all.sh"
echo ""
echo "3. Start with the new configuration:"
echo "   ./start-with-ui.sh"
echo ""
echo "4. Access the application at:"
echo "   http://${IP_ADDRESS}:3000"
echo ""
echo "Note: Make sure your firewall allows connections on ports 3000 and 8080-8083"
echo ""


