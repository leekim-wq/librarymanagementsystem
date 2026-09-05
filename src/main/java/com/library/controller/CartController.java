package com.library.controller;

import com.library.model.Book;
import com.library.model.Member;
import com.library.service.BookService;
import com.library.service.CartService;
import com.library.service.MemberService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cart")
public class CartController {

    private static final Logger log = LoggerFactory.getLogger(CartController.class);

    @Autowired
    private CartService cartService;

    @Autowired
    private BookService bookService;

    @Autowired
    private MemberService memberService;

    @GetMapping
    public String viewCart(HttpSession session, Model model) {
        model.addAttribute("cartItems", cartService.getCart(session));
        return "cart";
    }

    @PostMapping("/add/{bookId}")
    public String addToCart(@PathVariable Long bookId, HttpSession session,
                            RedirectAttributes redirectAttributes) {
        try {
            Book book = bookService.getBookById(bookId)
                    .orElseThrow(() -> new RuntimeException("Book not found"));
            cartService.addToCart(session, book);
            redirectAttributes.addFlashAttribute("success", "Book added to cart!");
        } catch (Exception e) {
            log.error("Error adding to cart", e);
            redirectAttributes.addFlashAttribute("error", "Failed to add book to cart.");
        }
        return "redirect:/books/" + bookId;
    }

    @PostMapping("/remove/{bookId}")
    public String removeFromCart(@PathVariable Long bookId, HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        cartService.removeFromCart(session, bookId);
        redirectAttributes.addFlashAttribute("success", "Book removed from cart.");
        return "redirect:/cart";
    }

    @PostMapping("/clear")
    public String clearCart(HttpSession session, RedirectAttributes redirectAttributes) {
        cartService.clearCart(session);
        redirectAttributes.addFlashAttribute("success", "Cart cleared.");
        return "redirect:/cart";
    }

    @PostMapping("/checkout")
    public String checkout(HttpSession session, Authentication authentication,
                           RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        try {
            Member member = (Member) authentication.getPrincipal();
            Member freshMember = memberService.getMemberById(member.getId())
                    .orElseThrow(() -> new RuntimeException("Member not found"));

            var cartItems = cartService.getCart(session);
            if (cartItems.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Your cart is empty.");
                return "redirect:/cart";
            }

            int successCount = 0;
            int failCount = 0;
            StringBuilder errors = new StringBuilder();

            for (var item : cartItems) {
                try {
                    boolean borrowed = bookService.borrowBook(item.getBook().getId(), freshMember);
                    if (borrowed) {
                        successCount++;
                    } else {
                        failCount++;
                        errors.append("• ").append(item.getBook().getTitle()).append(" – unavailable or limit reached.\n");
                    }
                } catch (Exception e) {
                    log.error("Error borrowing book: " + item.getBook().getId(), e);
                    failCount++;
                    errors.append("• ").append(item.getBook().getTitle()).append(" – system error.\n");
                }
            }

            cartService.clearCart(session);

            if (failCount == 0) {
                redirectAttributes.addFlashAttribute("success",
                        "All " + successCount + " books borrowed successfully!");
            } else {
                String msg = successCount + " borrowed, but " + failCount + " failed.\n" + errors.toString();
                redirectAttributes.addFlashAttribute("warning", msg);
            }
            return "redirect:/loans";

        } catch (Exception e) {
            log.error("Checkout failed", e);
            redirectAttributes.addFlashAttribute("error", "Checkout failed: " + e.getMessage());
            return "redirect:/cart";
        }
    }
}