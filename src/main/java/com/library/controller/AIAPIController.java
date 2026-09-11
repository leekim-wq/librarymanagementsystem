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

    @Autowired private AIService aiService;
    @Autowired private BookService bookService;

    @PostMapping("/recommend")
    public ResponseEntity<Map<String, Object>> getRecommendations(
            @RequestBody Map<String, Object> request) {

        String query = (String) request.get("query");
        List<Book> availableBooks = bookService.getAvailableBooks();
        List<Book> recommendations = aiService.getAIRecommendations(query, availableBooks);

        Map<String, Object> response = new HashMap<>();
        response.put("query", query);
        response.put("count", recommendations.size());
        response.put("found", !recommendations.isEmpty());
        response.put("recommendations", recommendations);

        if (recommendations.isEmpty()) {
            response.put("message",
                    "No books found matching \"" + query +
                            "\". Try searching by title, author, or category.");
        }

        return ResponseEntity.ok(response);
    }
}