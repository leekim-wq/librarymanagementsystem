package com.library.controller;

import com.library.model.Book;
import com.library.service.BookService;
import com.library.service.CartService;
import com.library.service.MemberService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private BookService bookService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private CartService cartService;

    @GetMapping("/")
    public String home(HttpSession session, Model model) {
        // Total copies (sum of all quantities)
        long totalCopies = bookService.getTotalCopies();
        model.addAttribute("totalBooks", totalCopies);   // reuse attribute name

        // Available copies (sum of availableQuantity)
        long availableCopies = bookService.getAvailableCopies();
        model.addAttribute("availableBooks", availableCopies);

        // Active members
        long totalMembers = memberService.getAllMembers().size();
        model.addAttribute("totalMembers", totalMembers);

        // Recommended books (top 5 most borrowed)
        List<Book> recommended = bookService.getMostBorrowedBooks();
        if (recommended.size() > 5) {
            recommended = recommended.subList(0, 5);
        }
        model.addAttribute("recommendedBooks", recommended);

        // Cart count
        model.addAttribute("cartCount", cartService.getCartCount(session));

        return "home";
    }
}