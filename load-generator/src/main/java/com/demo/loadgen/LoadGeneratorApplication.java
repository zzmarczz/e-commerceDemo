package com.demo.loadgen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LoadGeneratorApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(LoadGeneratorApplication.class, args);
        
        System.out.println("\n" +
            "╔════════════════════════════════════════════════════════════════╗\n" +
            "║                   Load Generator Started                       ║\n" +
            "╚════════════════════════════════════════════════════════════════╝\n" +
            "\n" +
            "Generating continuous load for APM monitoring...\n" +
            "Control Panel: http://localhost:9090/control\n" +
            "Statistics: http://localhost:9090/stats\n" +
            "\n" +
            "Press Ctrl+C to stop\n"
        );
    }
}


