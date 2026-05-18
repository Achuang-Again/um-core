package com.um.core.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = "com.um.core")
@EnableAsync
public class UmCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(UmCoreApplication.class, args);
    }
}
