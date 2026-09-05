package com.library.service;

import com.library.model.Book;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AIService {

    public List<Book> getAIRecommendations(String query, List<Book> availableBooks) {
        if (query == null || query.trim().isEmpty() || availableBooks.isEmpty()) {
            return availableBooks.stream().limit(5).collect(Collectors.toList());
        }

        String searchTerm = query.toLowerCase().trim();

        List<Book> results = availableBooks.stream()
                .filter(book ->
                        book.getTitle().toLowerCase().contains(searchTerm) ||
                                book.getAuthor().toLowerCase().contains(searchTerm) ||
                                (book.getCategory() != null && book.getCategory().toLowerCase().contains(searchTerm))
                )
                .collect(Collectors.toList());

        if (results.isEmpty()) {
            return availableBooks.stream()
                    .sorted((b1, b2) -> Double.compare(b2.getRating(), b1.getRating()))
                    .limit(5)
                    .collect(Collectors.toList());
        }

        return results.stream().limit(5).collect(Collectors.toList());
    }
}