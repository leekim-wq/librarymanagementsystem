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
import java.util.regex.Pattern;

@Controller
public class AuthController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Password validation: min 8 chars, at least 1 uppercase, 1 lowercase, 1 digit, 1 special
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");

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
        // 1. Check username uniqueness
        if (memberService.usernameExists(member.getUsername())) {
            redirectAttributes.addFlashAttribute("error", "Username already taken!");
            return "redirect:/register";
        }

        // 2. Check email uniqueness
        if (memberService.memberExists(member.getEmail())) {
            redirectAttributes.addFlashAttribute("error", "Email already registered!");
            return "redirect:/register";
        }

        // 3. Validate password strength
        if (!isValidPassword(member.getPassword())) {
            redirectAttributes.addFlashAttribute("error",
                    "Password must be at least 8 characters, include uppercase, lowercase, digit, and special character.");
            return "redirect:/register";
        }

        // 4. Encode password
        member.setPassword(passwordEncoder.encode(member.getPassword()));
        member.setMembershipDate(LocalDate.now());
        member.setRole("MEMBER");
        member.setActive(true);
        member.setBorrowingLimit(5);
        member.setTotalFines(0.0);

        // Save user
        memberService.saveMember(member);

        redirectAttributes.addFlashAttribute("success", "Registration successful! Please login.");
        return "redirect:/login";
    }

    private boolean isValidPassword(String password) {
        if (password == null) return false;
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }
}