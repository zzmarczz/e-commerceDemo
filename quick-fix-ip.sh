#!/bin/bash

# Quick IP Fix Script
# Run this on your server to configure for the current IP

echo "╔════════════════════════════════════════╗"
echo "║     Quick IP Configuration Fix         ║"
echo "╚════════════════════════════════════════╝"
echo ""

# The IP from your error: 3.10.224.202
IP_ADDRESS="3.10.224.202"

echo "Configuring for IP: $IP_ADDRESS"
echo ""

# 1. Configure services to bind to all interfaces
echo "Step 1: Configuring services to accept network connections..."
for props_file in product-service/src/main/resources/application.properties \
                  cart-service/src/main/resources/application.properties \
                  order-service/src/main/resources/application.properties \
                  api-gateway/src/main/resources/application.properties \
                  frontend/src/main/resources/application.properties; do
    if [ -f "$props_file" ]; then
        if ! grep -q "server.address" "$props_file"; then
            echo "server.address=0.0.0.0" >> "$props_file"
        else
            sed -i.bak 's/server.address=.*/server.address=0.0.0.0/' "$props_file"
        fi
        echo "  ✓ $(dirname $props_file | cut -d'/' -f1) configured"
    fi
done

# 2. Update CORS
echo ""
echo "Step 2: Updating CORS configuration..."
cat > api-gateway/src/main/java/com/demo/gateway/config/CorsConfig.java << EOF
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
        
        // Allow requests from any origin
        config.addAllowedOriginPattern("http://*:3000");
        config.addAllowedOriginPattern("https://*:3000");
        
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
echo "  ✓ CORS configured"

# 3. Update Frontend API URL
echo ""
echo "Step 3: Updating frontend API URL..."
cat > frontend/src/main/resources/static/js/app.js.head << 'EOF'
// E-Commerce Demo - Frontend Application

// Configuration - Get API URL from current location
const API_BASE_URL = window.location.protocol + '//' + window.location.hostname + ':8080/api';
let currentUserId = 'user123';
let cartData = null;
EOF

# Get the rest of the file (everything after line 6)
tail -n +7 frontend/src/main/resources/static/js/app.js > frontend/src/main/resources/static/js/app.js.tail

# Combine
cat frontend/src/main/resources/static/js/app.js.head frontend/src/main/resources/static/js/app.js.tail > frontend/src/main/resources/static/js/app.js.new
mv frontend/src/main/resources/static/js/app.js.new frontend/src/main/resources/static/js/app.js
rm frontend/src/main/resources/static/js/app.js.head frontend/src/main/resources/static/js/app.js.tail

echo "  ✓ Frontend will auto-detect API URL"

# 4. Update Gateway service URLs
echo ""
echo "Step 4: Updating API Gateway to use localhost for backend services..."
cat > api-gateway/src/main/resources/application.properties << EOF
server.port=8080
spring.application.name=api-gateway
server.address=0.0.0.0

# Microservices URLs (use localhost since they're on same machine)
services.product.url=http://localhost:8081
services.cart.url=http://localhost:8082
services.order.url=http://localhost:8083

# Logging
logging.level.com.demo.gateway=DEBUG
EOF
echo "  ✓ API Gateway configured"

# Clean up backup files
find . -name "*.bak" -type f -delete 2>/dev/null

echo ""
echo "════════════════════════════════════════"
echo "✓ Configuration Complete!"
echo "════════════════════════════════════════"
echo ""
echo "Next steps:"
echo ""
echo "1. Rebuild:"
echo "   mvn clean install -DskipTests"
echo ""
echo "2. Stop services:"
echo "   ./stop-all.sh"
echo ""
echo "3. Start services:"
echo "   ./start-with-ui.sh"
echo ""
echo "4. Wait 60 seconds"
echo ""
echo "5. Test API Gateway:"
echo "   curl http://localhost:8080/api/health"
echo ""
echo "6. Open browser:"
echo "   http://$IP_ADDRESS:3000"
echo ""
echo "════════════════════════════════════════"
echo ""
echo "AWS Security Group Requirements:"
echo "  - Port 3000  (Frontend)"
echo "  - Port 8080  (API Gateway)"
echo "  - Source: 0.0.0.0/0 or your IP"
echo ""


