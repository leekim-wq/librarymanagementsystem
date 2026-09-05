package com.library.controller;

import com.library.model.Book;
import com.library.model.Member;
import com.library.service.BookService;
import com.library.service.LoanService;
import com.library.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/books")
public class BookController {

    @Autowired
    private BookService bookService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private LoanService loanService;

    // ---------- LIST BOOKS ----------
    @GetMapping
    public String listBooks(@RequestParam(required = false) String query,
                            @RequestParam(required = false) String category,
                            Authentication authentication,
                            Model model) {
        List<Book> books;
        if (query != null && !query.trim().isEmpty()) {
            books = bookService.searchBooks(query);
        } else if (category != null && !category.trim().isEmpty()) {
            books = bookService.getBooksByCategory(category);
        } else {
            books = bookService.getAllBooks();
        }

        model.addAttribute("books", books);
        model.addAttribute("categories", bookService.getAllCategories());
        model.addAttribute("selectedCategory", category);
        model.addAttribute("query", query);

        if (authentication != null && authentication.isAuthenticated()) {
            Member member = (Member) authentication.getPrincipal();
            model.addAttribute("member", member);
        }

        // ✅ CHANGE: return "books" instead of "book-list"
        return "books";
    }

    // ---------- BOOK DETAIL ----------
    @GetMapping("/{id}")
    public String viewBook(@PathVariable Long id, Model model, Authentication authentication) {
        Book book = bookService.getBookById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));
        model.addAttribute("book", book);

        if (authentication != null && authentication.isAuthenticated()) {
            Member member = (Member) authentication.getPrincipal();
            model.addAttribute("member", member);
        }

        // This already matches your "book-detail.html"
        return "book-detail";
    }

    // ---------- BORROW ----------
    @PostMapping("/borrow/{bookId}")
    public String borrowBook(@PathVariable Long bookId,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Member member = (Member) authentication.getPrincipal();
        Member freshMember = memberService.getMemberById(member.getId())
                .orElseThrow(() -> new RuntimeException("Member not found"));

        boolean success = bookService.borrowBook(bookId, freshMember);
        if (success) {
            redirectAttributes.addFlashAttribute("success", "Book borrowed successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to borrow book. It may be unavailable or you may have reached your limit.");
        }
        return "redirect:/books/" + bookId;
    }

    // ---------- RETURN ----------
    @PostMapping("/return/{loanId}")
    public String returnBook(@PathVariable Long loanId,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Member member = (Member) authentication.getPrincipal();

        boolean belongsToMember = loanService.isLoanBelongsToMember(loanId, member.getId());
        if (!belongsToMember) {
            redirectAttributes.addFlashAttribute("error", "Unauthorized access to this loan.");
            return "redirect:/loans";
        }

        boolean success = bookService.returnBook(loanId);
        if (success) {
            redirectAttributes.addFlashAttribute("success", "Book returned successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to return book. Please try again.");
        }
        return "redirect:/loans";
    }
}