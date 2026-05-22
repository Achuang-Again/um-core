package com.um.core.bootstrap;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = "com.um.core")
@EnableAsync
public class UmCoreApplication {

    public static void main(String[] args) {
        // Load environment variables from .env.properties file
        // Try multiple paths: current dir (IDE/JAR), parent dir (mvn spring-boot:run from module)
        loadDotEnv();
        SpringApplication.run(UmCoreApplication.class, args);
    }

    private static void loadDotEnv() {
        String[] searchPaths = {"./", "../"};
        for (String path : searchPaths) {
            try {
                Dotenv dotenv = Dotenv.configure()
                        .directory(path)
                        .filename(".env.properties")
                        .load();
                dotenv.entries().forEach(entry ->
                        System.setProperty(entry.getKey(), entry.getValue()));
                System.out.println("Loaded .env.properties from: " + path);
                return;
            } catch (Exception ignored) {
                // Try next path
            }
        }
        System.err.println("Warning: Could not load .env.properties from ./ or ../");
    }
}
