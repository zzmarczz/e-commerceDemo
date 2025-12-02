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
        
        // Allow requests from frontend (any IP on port 3000)
        config.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:3000",
            "http://127.0.0.1:3000",
            "http://*:3000"  // Allow any IP on port 3000 (for EC2/cloud deployments)
        ));
        
        // Allow all HTTP methods
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // Allow all headers
        config.setAllowedHeaders(Arrays.asList("*"));
        
        // Expose custom headers to browser/JavaScript (required for CORS)
        config.setExposedHeaders(Arrays.asList(
            "X-Session-ID",
            "X-Journey-ID", 
            "X-Order-Id",
            "X-Order-Value",
            "X-Item-Count",
            "TotalRevenue",
            "TotalOrders",
            "AvgOrderValue"
        ));
        
        // Allow credentials
        config.setAllowCredentials(true);
        
        // Max age for preflight requests
        config.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
}


