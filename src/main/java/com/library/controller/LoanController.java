package com.library.controller;

import com.library.model.Loan;
import com.library.model.Member;
import com.library.service.BookService;
import com.library.service.LoanService;
import com.library.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

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
    @Transactional(readOnly = true)
    public String myLoans(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        Member member = (Member) authentication.getPrincipal();
        Member freshMember = memberService.getMemberById(member.getId())
                .orElseThrow(() -> new RuntimeException("Member not found"));

        List<Loan> loans = loanService.getAllLoansForMember(freshMember.getId());
        model.addAttribute("loans", loans);
        return "loans";
    }

    /**
     * Return a book – only Librarians and Admins can perform this action.
     * The ownership check is removed because they are allowed to return any loan.
     */
    @PostMapping("/return/{loanId}")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")   // 🔒 only these roles
    public String returnBook(@PathVariable Long loanId,
                             RedirectAttributes redirectAttributes) {
        boolean success = bookService.returnBook(loanId);
        if (success) {
            redirectAttributes.addFlashAttribute("success", "Book returned successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to return book. Please try again.");
        }
        return "redirect:/loans";
    }

    // other methods (borrow, active, etc.) remain unchanged
}