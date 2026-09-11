package com.library.controller;

import com.library.model.Member;
import com.library.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
public class AuthController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid email/username or password!");
        }
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully.");
        }
        return "login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("member", new Member());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(Member member, RedirectAttributes redirectAttributes) {

        // Duplicate email check
        if (memberService.memberExists(member.getEmail())) {
            redirectAttributes.addFlashAttribute("error", "Email already registered!");
            return "redirect:/register";
        }

        // Username handling — auto-generate from email if empty
        String username = member.getUsername();
        if (username == null || username.trim().isEmpty()) {
            username = member.getEmail().split("@")[0];
        }
        if (memberService.usernameExists(username)) {
            // Make it unique by appending a number
            String base = username;
            int n = 1;
            while (memberService.usernameExists(base + n)) n++;
            username = base + n;
        }
        member.setUsername(username);

        // Password validation
        if (member.getPassword() == null || member.getPassword().length() < 6) {
            redirectAttributes.addFlashAttribute("error", "Password must be at least 6 characters!");
            return "redirect:/register";
        }

        // Encode password
        member.setPassword(passwordEncoder.encode(member.getPassword()));
        member.setMembershipDate(LocalDate.now());
        member.setRole("MEMBER");
        member.setActive(true);
        member.setBorrowingLimit(5);
        member.setTotalFines(0.0);

        memberService.saveMember(member);

        redirectAttributes.addFlashAttribute("success",
                "Registration successful! You can now log in with your email or username.");
        return "redirect:/login";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }
}