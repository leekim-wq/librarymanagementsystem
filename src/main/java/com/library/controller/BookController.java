package com.library.controller;

import com.library.model.Book;
import com.library.model.Member;
import com.library.service.BookService;
import com.library.service.MemberService;
import jakarta.servlet.http.HttpSession;
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
    public String home(Model model, HttpSession session) {
        // Add member to session for testing (remove in production)
        if (session.getAttribute("member") == null) {
            // Create a test member if none exists
            Member testMember = memberService.getMemberByEmail("test@library.com")
                    .orElseGet(() -> {
                        Member newMember = new Member();
                        newMember.setEmail("test@library.com");
                        newMember.setName("Test User");
                        newMember.setPassword("password");
                        newMember.setMembershipDate(java.time.LocalDate.now());
                        return memberService.saveMember(newMember);
                    });
            session.setAttribute("member", testMember);
        }

        model.addAttribute("totalBooks", bookService.getAllBooks().size());
        model.addAttribute("availableBooks", bookService.getAvailableBooks().size());
        model.addAttribute("totalMembers", memberService.getAllMembers().size());
        model.addAttribute("recommendedBooks", bookService.getAIRecommendations(""));
        return "home";
    }

    // Book list page with search
    @GetMapping("/books")
    public String listBooks(@RequestParam(required = false) String category,
                            @RequestParam(required = false) String search,
                            Model model) {
        List<Book> books;

        if (search != null && !search.isEmpty()) {
            books = bookService.searchBooks(search);
            model.addAttribute("searchQuery", search);
        } else if (category != null && !category.isEmpty()) {
            books = bookService.getBooksByCategory(category);
            model.addAttribute("selectedCategory", category);
        } else {
            books = bookService.getAllBooks();
        }

        model.addAttribute("books", books);
        model.addAttribute("categories", bookService.getAllCategories());
        return "books";
    }

    // Book detail page
    @GetMapping("/books/{id}")
    public String bookDetail(@PathVariable Long id, Model model, HttpSession session) {
        Book book = bookService.getBookById(id).orElse(null);
        if (book == null) {
            return "redirect:/books";
        }
        model.addAttribute("book", book);
        model.addAttribute("isLoggedIn", session.getAttribute("member") != null);
        return "book-detail";
    }

    // Borrow a book
    @PostMapping("/books/borrow/{id}")
    public String borrowBook(@PathVariable Long id,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        Member member = (Member) session.getAttribute("member");
        if (member == null) {
            redirectAttributes.addFlashAttribute("error", "Please login first!");
            return "redirect:/login";
        }

        boolean success = bookService.borrowBook(id, member);
        if (success) {
            redirectAttributes.addFlashAttribute("success", "Book borrowed successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to borrow book. It may be unavailable or you've reached your limit.");
        }
        return "redirect:/books/" + id;
    }

    // Return a book
    @PostMapping("/books/return/{loanId}")
    public String returnBook(@PathVariable Long loanId,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        Member member = (Member) session.getAttribute("member");
        if (member == null) {
            redirectAttributes.addFlashAttribute("error", "Please login first!");
            return "redirect:/login";
        }

        boolean success = bookService.returnBook(loanId);
        if (success) {
            redirectAttributes.addFlashAttribute("success", "Book returned successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to return book.");
        }
        return "redirect:/loans";
    }
}