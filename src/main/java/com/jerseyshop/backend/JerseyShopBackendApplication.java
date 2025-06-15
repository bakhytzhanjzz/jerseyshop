package com.jerseyshop.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class JerseyShopBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(JerseyShopBackendApplication.class, args);
    }
}
