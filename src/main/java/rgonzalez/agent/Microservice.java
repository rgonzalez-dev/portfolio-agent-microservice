package rgonzalez.agent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Portfolio Agent Microservice.
 *
 * This is a Spring Boot 4.0.1 microservice application built with Java 17.
 * It provides RESTful APIs for portfolio agent management with security,
 * persistence, and monitoring capabilities.
 */
@SpringBootApplication
public class Microservice {

    public static void main(String[] args) {
        SpringApplication.run(Microservice.class, args);
    }
}
