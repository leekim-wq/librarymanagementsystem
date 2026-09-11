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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/loans")
public class LoanController {

    @Autowired private LoanService loanService;
    @Autowired private BookService bookService;
    @Autowired private MemberService memberService;

    // ============================================================
    // MY LOANS — /loans
    // ============================================================
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

        List<Loan> activeLoans = new ArrayList<>();
        List<Loan> historyLoans = new ArrayList<>();
        double totalFines = 0.0;

        for (Loan loan : loans) {
            if (loan.isReturned()) {
                historyLoans.add(loan);
            } else {
                activeLoans.add(loan);
                if (loan.isOverdue()) {
                    totalFines += loan.calculateFine();
                }
            }
        }

        model.addAttribute("member", freshMember);
        model.addAttribute("loans", loans);
        model.addAttribute("activeLoans", activeLoans);
        model.addAttribute("historyLoans", historyLoans);
        model.addAttribute("activeLoansCount", activeLoans.size());
        model.addAttribute("totalFines", totalFines);
        model.addAttribute("borrowingLimit", freshMember.getBorrowingLimit());
        model.addAttribute("availableSlots",
                Math.max(0, freshMember.getBorrowingLimit() - activeLoans.size()));
        model.addAttribute("today", LocalDate.now());

        return "my-loans";
    }

    // ============================================================
    // MANAGE LOANS — /loans/manage  (STAFF ONLY)
    // ⚠️  @Transactional(readOnly = true) is REQUIRED so the template
    //     can safely read lazy-loaded loan.book and loan.member.
    // ============================================================
    @GetMapping("/manage")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    @Transactional(readOnly = true)
    public String manageLoans(Model model) {
        List<Loan> loans = loanService.getAllActiveLoans();

        LocalDate today = LocalDate.now();
        LocalDate threeDaysOut = today.plusDays(3);

        long overdueCount = 0;
        long dueSoonCount = 0;

        for (Loan loan : loans) {
            if (loan.isOverdue()) {
                overdueCount++;
            } else if (loan.getDueDate() != null
                    && !loan.getDueDate().isAfter(threeDaysOut)) {
                dueSoonCount++;
            }
        }

        model.addAttribute("loans", loans);
        model.addAttribute("totalLoans", loans.size());
        model.addAttribute("overdueCount", overdueCount);
        model.addAttribute("dueSoonCount", dueSoonCount);
        model.addAttribute("today", today);

        return "loans-manage";
    }

    // ============================================================
    // RETURN — STAFF ONLY
    // ============================================================
    @PostMapping("/return/{loanId}")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public String returnBook(@PathVariable Long loanId,
                             RedirectAttributes redirectAttributes) {
        boolean success = loanService.returnBook(loanId);

        if (success) {
            redirectAttributes.addFlashAttribute("success", "Book returned successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error",
                    "Failed to return book. It may already be returned.");
        }
        return "redirect:/loans/manage";
    }
}