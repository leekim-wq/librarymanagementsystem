package com.library.controller;

import com.library.model.Member;
import com.library.service.BookService;
import com.library.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private BookService bookService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalUsers", memberService.getAllMembers().size());
        model.addAttribute("totalBooks", bookService.getAllBooks().size());
        model.addAttribute("availableBooks", bookService.getAvailableBooks().size());
        model.addAttribute("users", memberService.getAllMembers());
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String manageUsers(Model model) {
        model.addAttribute("users", memberService.getAllMembers());
        return "admin/users";
    }

    @PostMapping("/users/update-role")
    public String updateUserRole(@RequestParam Long userId,
                                 @RequestParam String role,
                                 RedirectAttributes redirectAttributes) {
        Member member = memberService.getMemberById(userId).orElse(null);
        if (member != null) {
            member.setRole(role);
            memberService.saveMember(member);
            redirectAttributes.addFlashAttribute("success", "User role updated successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "User not found!");
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/activate")
    public String activateUser(@RequestParam Long userId,
                               @RequestParam boolean active,
                               RedirectAttributes redirectAttributes) {
        Member member = memberService.getMemberById(userId).orElse(null);
        if (member != null) {
            member.setActive(active);
            memberService.saveMember(member);
            redirectAttributes.addFlashAttribute("success",
                    active ? "User activated successfully!" : "User deactivated successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "User not found!");
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/delete")
    public String deleteUser(@RequestParam Long userId, RedirectAttributes redirectAttributes) {
        try {
            memberService.deleteMember(userId);
            redirectAttributes.addFlashAttribute("success", "User deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete user!");
        }
        return "redirect:/admin/users";
    }
}