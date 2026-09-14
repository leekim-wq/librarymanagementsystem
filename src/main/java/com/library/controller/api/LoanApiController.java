package com.library.controller.api;

import com.library.model.Member;
import com.library.repository.MemberRepository;
import com.library.security.JwtUtil;
import com.library.service.BookService;
import com.library.service.LoanService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/loans")
public class LoanApiController {

    @Autowired private BookService bookService;
    @Autowired private LoanService loanService;
    @Autowired private MemberRepository memberRepository;
    @Autowired private JwtUtil jwtUtil;

    private Member getMember(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        Long memberId = jwtUtil.getMemberId(token);
        return memberRepository.findById(memberId).orElseThrow();
    }

    @GetMapping
    public ResponseEntity<?> myLoans(HttpServletRequest request) {
        Member member = getMember(request);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "loans", loanService.getAllLoansForMember(member.getId())
        ));
    }

    @PostMapping("/borrow")
    public ResponseEntity<?> borrow(@RequestBody Map<String, Long> body,
                                    HttpServletRequest request) {
        Member member = getMember(request);
        boolean ok = bookService.borrowBook(body.get("bookId"), member);
        return ResponseEntity.ok(Map.of(
                "success", ok,
                "message", ok ? "Book borrowed" : "Failed to borrow"
        ));
    }

    @PostMapping("/return/{loanId}")
    public ResponseEntity<?> returnBook(@PathVariable Long loanId,
                                        HttpServletRequest request) {
        getMember(request); // authorize
        boolean ok = loanService.returnBook(loanId);
        return ResponseEntity.ok(Map.of("success", ok));
    }
}