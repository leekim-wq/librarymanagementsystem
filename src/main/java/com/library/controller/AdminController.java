package com.library.controller;

import com.library.model.Member;
import com.library.service.BookService;
import com.library.service.LoanService;
import com.library.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private BookService bookService;

    @Autowired
    private LoanService loanService;

    // ============================================================
    // DASHBOARD
    // ============================================================
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<Member> allUsers = memberService.getAllMembers();

        long totalUsers = allUsers.size();
        long adminCount = allUsers.stream()
                .filter(u -> "ADMIN".equals(u.getRole()))
                .count();
        long librarianCount = allUsers.stream()
                .filter(u -> "LIBRARIAN".equals(u.getRole()))
                .count();
        long activeLoans = loanService.getAllActiveLoans().size();

        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("adminCount", adminCount);
        model.addAttribute("librarianCount", librarianCount);
        model.addAttribute("activeLoans", activeLoans);
        model.addAttribute("users", allUsers);

        // Recent active loans (5 most recent)
        model.addAttribute("recentLoans",
                loanService.getAllActiveLoans().stream().limit(5).toList());

        // Chart data
        model.addAttribute("chartLabels", List.of(
                "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"));
        model.addAttribute("chartData", List.of(
                8, 12, 15, 22, 18, 24, 30, 28, 32, 40, 45, 52));

        return "admin/dashboard";
    }

    // ============================================================
    // USERS
    // ============================================================
    @GetMapping("/users")
    public String manageUsers(Model model) {
        model.addAttribute("users", memberService.getAllMembers());
        return "admin/users";
    }

    @PostMapping("/users/update-role")
    public String updateUserRole(@RequestParam Long userId,
                                 @RequestParam String role,
                                 RedirectAttributes ra) {
        Member member = memberService.getMemberById(userId).orElse(null);
        if (member != null) {
            member.setRole(role);
            memberService.saveMember(member);
            ra.addFlashAttribute("success", "User role updated successfully!");
        } else {
            ra.addFlashAttribute("error", "User not found!");
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/activate")
    public String activateUser(@RequestParam Long userId,
                               @RequestParam boolean active,
                               RedirectAttributes ra) {
        Member member = memberService.getMemberById(userId).orElse(null);
        if (member != null) {
            member.setActive(active);
            memberService.saveMember(member);
            ra.addFlashAttribute("success",
                    active ? "User activated!" : "User deactivated!");
        } else {
            ra.addFlashAttribute("error", "User not found!");
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/delete")
    public String deleteUser(@RequestParam Long userId, RedirectAttributes ra) {
        try {
            memberService.deleteMember(userId);
            ra.addFlashAttribute("success", "User deleted successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Failed to delete user!");
        }
        return "redirect:/admin/users";
    }
}