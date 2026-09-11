package com.library.service;

import com.library.model.Book;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Local, deterministic AI-style book recommendation engine.
 *
 * It does NOT call any external API. Instead, it scores every available book
 * against the user's query and returns the best matches.
 *
 * Key guarantees:
 *   - If nothing matches, returns an EMPTY list (so the UI can say "not found").
 *   - Never pads the results with random books.
 *   - Results are ordered by relevance, then by rating.
 */
@Service
public class AIService {

    private static final int MAX_RESULTS = 5;

    /**
     * Get recommendations for a free-text query.
     *
     * @param query          user's natural language query (e.g. "clean code")
     * @param availableBooks books currently available for borrowing
     * @return best matching books (max 5), or empty list if nothing matches
     */
    public List<Book> getAIRecommendations(String query, List<Book> availableBooks) {

        // No books at all → return empty
        if (availableBooks == null || availableBooks.isEmpty()) {
            return Collections.emptyList();
        }

        // Empty query → return top-rated available books (used on home page)
        if (query == null || query.trim().isEmpty()) {
            return availableBooks.stream()
                    .sorted(Comparator.comparing(
                            b -> b.getRating() == null ? 0.0 : b.getRating(),
                            Comparator.reverseOrder()))
                    .limit(MAX_RESULTS)
                    .collect(Collectors.toList());
        }

        // Tokenize query into keywords
        String[] keywords = query.toLowerCase().trim().split("\\s+");

        // Score every available book
        List<ScoredBook> scored = new ArrayList<>();
        for (Book book : availableBooks) {
            int score = scoreBook(book, keywords);
            if (score > 0) {
                scored.add(new ScoredBook(book, score));
            }
        }

        // Nothing matched → return empty (so UI can say "not found")
        if (scored.isEmpty()) {
            return Collections.emptyList();
        }

        // Sort by score desc, then by rating desc
        scored.sort((a, b) -> {
            int cmp = Integer.compare(b.score, a.score);
            if (cmp != 0) return cmp;
            double r1 = a.book.getRating() == null ? 0.0 : a.book.getRating();
            double r2 = b.book.getRating() == null ? 0.0 : b.book.getRating();
            return Double.compare(r2, r1);
        });

        // Return top N — no padding
        return scored.stream()
                .limit(MAX_RESULTS)
                .map(sb -> sb.book)
                .collect(Collectors.toList());
    }

    /**
     * Score a single book against the user's keywords.
     * Higher score = better match.
     */
    private int scoreBook(Book book, String[] keywords) {
        int score = 0;

        String title       = safeLower(book.getTitle());
        String author      = safeLower(book.getAuthor());
        String category    = safeLower(book.getCategory());
        String description = safeLower(book.getDescription());

        for (String kw : keywords) {
            if (kw.isEmpty()) continue;

            if (title.contains(kw))       score += 10;   // title match (highest)
            if (title.startsWith(kw))     score += 5;    // title prefix bonus
            if (author.contains(kw))      score += 8;    // author match
            if (category.contains(kw))    score += 6;    // category match
            if (description.contains(kw)) score += 3;    // description match
        }

        return score;
    }

    private String safeLower(String s) {
        return s == null ? "" : s.toLowerCase();
    }

    private static class ScoredBook {
        final Book book;
        final int score;
        ScoredBook(Book book, int score) {
            this.book = book;
            this.score = score;
        }
    }
}