package com.library.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.model.Book;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class AIService {

    @Value("${ai.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Book> getAIRecommendations(String userQuery, List<Book> availableBooks) {
        try {
            // If no books available or no query, return all books
            if (availableBooks.isEmpty() || userQuery == null || userQuery.trim().isEmpty()) {
                return availableBooks.stream().limit(5).toList();
            }

            // Create a prompt for the AI
            String prompt = buildRecommendationPrompt(userQuery, availableBooks);

            // Call Gemini API
            String aiResponse = callGeminiAPI(prompt);

            // Parse AI response to get book IDs
            List<Long> recommendedIds = parseAIResponse(aiResponse);

            // Return recommended books in order
            Map<Long, Book> bookMap = new HashMap<>();
            for (Book book : availableBooks) {
                bookMap.put(book.getId(), book);
            }

            List<Book> recommendations = new ArrayList<>();
            for (Long id : recommendedIds) {
                if (bookMap.containsKey(id)) {
                    recommendations.add(bookMap.get(id));
                }
            }

            // If AI didn't return enough recommendations, add more books
            if (recommendations.size() < 5) {
                availableBooks.stream()
                        .filter(book -> !recommendations.contains(book))
                        .limit(5 - recommendations.size())
                        .forEach(recommendations::add);
            }

            return recommendations;

        } catch (Exception e) {
            e.printStackTrace();
            // Fallback: return top available books
            return availableBooks.stream().limit(5).toList();
        }
    }

    private String buildRecommendationPrompt(String query, List<Book> books) {
        StringBuilder booksList = new StringBuilder();
        for (Book book : books) {
            booksList.append(String.format("ID: %d, Title: %s, Author: %s, Category: %s, Description: %s\n",
                    book.getId(), book.getTitle(), book.getAuthor(),
                    book.getCategory(), book.getDescription()));
        }

        return String.format("""
            You are a library recommendation system. Given the following books and a user query, 
            recommend the most relevant books by their ID numbers.
            
            User Query: "%s"
            
            Available Books:
            %s
            
            Return ONLY a JSON array of book IDs in order of relevance, e.g.: [5, 12, 3, 8, 1]
            Make sure to only include book IDs from the available books list.
            """, query, booksList.toString());
    }

    private String callGeminiAPI(String prompt) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=" + apiKey;

        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> contents = new HashMap<>();
        Map<String, String> part = new HashMap<>();
        part.put("text", prompt);
        contents.put("parts", new Object[]{part});
        requestBody.put("contents", new Object[]{contents});

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        }
        return null;
    }

    private List<Long> parseAIResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            String text = root.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();

            // Extract JSON array from the response
            int start = text.indexOf('[');
            int end = text.lastIndexOf(']') + 1;
            if (start >= 0 && end > start) {
                String jsonArray = text.substring(start, end);
                JsonNode array = objectMapper.readTree(jsonArray);

                List<Long> ids = new ArrayList<>();
                for (JsonNode node : array) {
                    ids.add(node.asLong());
                }
                return ids;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
}