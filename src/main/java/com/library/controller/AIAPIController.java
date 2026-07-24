package com.library.controller;

import com.library.model.Book;
import com.library.service.AIService;
import com.library.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AIAPIController {

    @Autowired
    private AIService aiService;

    @Autowired
    private BookService bookService;

    @PostMapping("/recommend")
    public ResponseEntity<Map<String, Object>> getRecommendations(@RequestBody Map<String, Object> request) {
        String query = (String) request.get("query");

        // Get all books for recommendations
        List<Book> allBooks = bookService.getAllBooks();
        List<Book> recommendations;

        if (query == null || query.trim().isEmpty()) {
            // Return random books if no query
            recommendations = allBooks.stream()
                    .limit(5)
                    .collect(java.util.stream.Collectors.toList());
        } else {
            // Search for books matching the query
            recommendations = bookService.searchBooks(query);

            // If no matches, return some random books
            if (recommendations.isEmpty()) {
                recommendations = allBooks.stream()
                        .limit(5)
                        .collect(java.util.stream.Collectors.toList());
            } else if (recommendations.size() > 5) {
                recommendations = recommendations.subList(0, 5);
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("recommendations", recommendations);
        response.put("count", recommendations.size());
        response.put("query", query);

        return ResponseEntity.ok(response);
    }
}