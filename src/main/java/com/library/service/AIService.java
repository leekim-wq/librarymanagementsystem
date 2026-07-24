package com.library.service;

import com.library.model.Book;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AIService {

    /**
     * Get AI-powered book recommendations based on user query
     */
    public List<Book> getAIRecommendations(String query, List<Book> availableBooks) {
        if (query == null || query.trim().isEmpty() || availableBooks.isEmpty()) {
            return availableBooks.stream().limit(5).collect(Collectors.toList());
        }

        String searchTerm = query.toLowerCase().trim();

        // Smart search: check title, author, and category
        List<Book> results = availableBooks.stream()
                .filter(book ->
                        book.getTitle().toLowerCase().contains(searchTerm) ||
                                book.getAuthor().toLowerCase().contains(searchTerm) ||
                                (book.getCategory() != null && book.getCategory().toLowerCase().contains(searchTerm))
                )
                .collect(Collectors.toList());

        // If no results, return top rated books
        if (results.isEmpty()) {
            return availableBooks.stream()
                    .sorted((b1, b2) -> b2.getRating().compareTo(b1.getRating()))
                    .limit(5)
                    .collect(Collectors.toList());
        }

        // Limit to 5 recommendations
        return results.stream().limit(5).collect(Collectors.toList());
    }
}