package com.library.controller;

import com.library.model.Book;
import com.library.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private BookRepository bookRepository;

    @GetMapping("/db")
    public Map<String, Object> testDatabaseConnection() {
        Map<String, Object> response = new HashMap<>();
        try {
            long count = bookRepository.count();
            response.put("status", "SUCCESS");
            response.put("message", "Connected to lib_db successfully!");
            response.put("totalBooks", count);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Failed to connect to database: " + e.getMessage());
        }
        return response;
    }
}