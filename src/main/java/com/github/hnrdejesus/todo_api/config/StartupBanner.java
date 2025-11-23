package com.github.hnrdejesus.todo_api.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Displays useful application information on startup.
 * Improves developer experience by showing access URLs and configuration.
 */
@Configuration
public class StartupBanner {

    private static final Logger log = LoggerFactory.getLogger(StartupBanner.class);

    @Bean
    public CommandLineRunner displayInfo(Environment environment) {
        return args -> {
            String port = environment.getProperty("server.port", "8080");
            String contextPath = environment.getProperty("server.servlet.context-path", "");

            log.info("""
                
                =================================================
                Application started successfully
                =================================================
                
                API Endpoints: http://localhost:%s%s/api/tasks
                H2 Console:    http://localhost:%s%s/h2-console
                
                Database: H2 (in-memory)
                  JDBC URL: jdbc:h2:mem:testdb
                  Username: sa
                  Password: (empty)
                
                Available Endpoints:
                  GET    /api/tasks                    - List all tasks
                  POST   /api/tasks                    - Create task
                  GET    /api/tasks/{id}               - Get task by ID
                  PUT    /api/tasks/{id}               - Update task
                  PATCH  /api/tasks/{id}/toggle        - Toggle completion
                  DELETE /api/tasks/{id}               - Delete task
                  GET    /api/tasks/stats              - Get statistics
                  GET    /api/tasks/search?keyword={}  - Search tasks
                
                =================================================
                """.formatted(port, contextPath, port, contextPath));
        };
    }
}