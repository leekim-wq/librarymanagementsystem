package com.library.controller;

import com.library.model.Member;
import com.library.service.BookService;
import com.library.service.LoanService;
import com.library.service.MemberService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/loans")
public class LoanController {

    @Autowired
    private LoanService loanService;

    @Autowired
    private BookService bookService;

    @Autowired
    private MemberService memberService;

    @GetMapping
    public String myLoans(HttpSession session, Model model) {
        Member member = (Member) session.getAttribute("member");
        if (member == null) {
            return "redirect:/login";
        }
        model.addAttribute("loans", loanService.getMemberLoans(member.getId()));
        return "loans";
    }

    @PostMapping("/borrow")
    public String borrowBook(@RequestParam Long bookId, HttpSession session,
                             RedirectAttributes redirectAttributes) {
        Member member = (Member) session.getAttribute("member");
        if (member == null) {
            return "redirect:/login";
        }

        boolean success = bookService.borrowBook(bookId, member);
        if (success) {
            redirectAttributes.addFlashAttribute("success", "Book borrowed successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to borrow book.");
        }
        return "redirect:/books/" + bookId;
    }

    @PostMapping("/return/{loanId}")
    public String returnBook(@PathVariable Long loanId, HttpSession session,
                             RedirectAttributes redirectAttributes) {
        Member member = (Member) session.getAttribute("member");
        if (member == null) {
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