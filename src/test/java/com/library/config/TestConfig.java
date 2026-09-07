package com.library.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestConfig {

    /**
     * Overrides any existing CommandLineRunner beans during tests.
     * This prevents any data seeding from running when the test context loads.
     */
    @Bean
    @Primary
    public CommandLineRunner noOpCommandLineRunner() {
        return args -> {
            // Do nothing – this replaces any DataLoader / CommandLineRunner
            System.out.println("🧪 Test mode: Data seeding disabled.");
        };
    }
}