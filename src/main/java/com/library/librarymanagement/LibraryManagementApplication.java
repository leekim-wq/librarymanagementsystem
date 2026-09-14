package com.library;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling   // Required for Firebase due-date reminders
public class LibraryManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(LibraryManagementApplication.class, args);

        System.out.println("=========================================");
        System.out.println("🚀 Smart Library System Started!");
        System.out.println("📚 Access at: http://localhost:8080");
        System.out.println("🔔 Firebase push notifications: enabled");
        System.out.println("=========================================");
    }
}