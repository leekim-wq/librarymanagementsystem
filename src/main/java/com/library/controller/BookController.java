package com.library.controller;

import com.library.model.Book;
import com.library.model.Member;
import com.library.service.BookService;
import com.library.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class BookController {

    @Autowired
    private BookService bookService;

    @Autowired
    private MemberService memberService;

    // Home page
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("totalBooks", bookService.getAllBooks().size());
        model.addAttribute("availableBooks", bookService.getAvailableBooks().size());
        model.addAttribute("totalMembers", memberService.getAllMembers().size());
        model.addAttribute("recommendedBooks", bookService.getAIRecommendations(""));
        return "home";
    }

    // Book list page
    @GetMapping("/books")
    public String listBooks(@RequestParam(required = false) String category, Model model) {
        List<Book> books;
        if (category != null && !category.isEmpty()) {
            books = bookService.getBooksByCategory(category);
        } else {
            books = bookService.getAllBooks();
        }
        model.addAttribute("books", books);
        model.addAttribute("categories", bookService.getAllCategories());
        return "books";
    }

    // Book detail page
    @GetMapping("/books/{id}")
    public String bookDetail(@PathVariable Long id, Model model) {
        Book book = bookService.getBookById(id).orElse(null);
        if (book == null) {
            return "redirect:/books";
        }
        model.addAttribute("book", book);
        return "book-detail";
    }

    // Search books
    @GetMapping("/books/search")
    public String searchBooks(@RequestParam String q, Model model) {
        List<Book> results = bookService.searchBooks(q);
        model.addAttribute("books", results);
        model.addAttribute("searchQuery", q);
        return "search-results";
    }
}