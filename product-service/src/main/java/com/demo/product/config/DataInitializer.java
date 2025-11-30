package com.demo.product.config;

import com.demo.product.model.Product;
import com.demo.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public void run(String... args) {
        // Initialize with sample products
        productRepository.save(new Product("Laptop", "High-performance laptop", 999.99, 10));
        productRepository.save(new Product("Mouse", "Wireless mouse", 29.99, 50));
        productRepository.save(new Product("Keyboard", "Mechanical keyboard", 79.99, 30));
        productRepository.save(new Product("Monitor", "27-inch 4K monitor", 399.99, 15));
        productRepository.save(new Product("Headphones", "Noise-cancelling headphones", 199.99, 25));
        
        System.out.println("Sample products initialized!");
    }
}


