package com.library.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("test")
public class TestConfig {

    /**
     * Override any CommandLineRunner that seeds data.
     * This prevents the username null error during tests.
     */
    @Bean
    public CommandLineRunner noOpCommandLineRunner() {
        return args -> {
            // Do nothing – this overrides the production CommandLineRunner
        };
    }
}