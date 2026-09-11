package com.library.controller;

import com.library.model.Book;
import com.library.model.Member;
import com.library.service.BookService;
import com.library.service.CartService;
import com.library.service.MemberService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
        // ----- Statistics -----
        long totalCopies = bookService.getTotalCopies();
        model.addAttribute("totalBooks", totalCopies);

        long availableCopies = bookService.getAvailableCopies();
        model.addAttribute("availableBooks", availableCopies);

        long totalMembers = memberService.getAllMembers().size();
        model.addAttribute("totalMembers", totalMembers);

        // ----- Recommended books (top 5 most borrowed) -----
        List<Book> recommended = bookService.getMostBorrowedBooks();
        if (recommended.size() > 5) {
            recommended = recommended.subList(0, 5);
        }
        model.addAttribute("recommendedBooks", recommended);

        // ----- Cart count -----
        model.addAttribute("cartCount", cartService.getCartCount(session));

        // ----- Logged-in user info (safe, no lazy access) -----
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getPrincipal())) {
            String email = auth.getName();
            memberService.getMemberByEmail(email).ifPresent(member -> {
                model.addAttribute("currentMember", member);
                // These run inside a transaction, so no LazyInitializationException
                model.addAttribute("activeLoansCount",
                        memberService.countActiveLoans(member));
                model.addAttribute("totalFines",
                        memberService.getTotalFines(member));
            });
        }

        return "home";
    }
}