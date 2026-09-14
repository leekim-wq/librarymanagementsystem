package com.library.controller.api;

import com.library.model.Member;
import com.library.repository.MemberRepository;
import com.library.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/devices")
public class DeviceController {

    @Autowired private MemberRepository memberRepository;
    @Autowired private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> registerDevice(@RequestBody Map<String, String> body,
                                            HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        Long memberId = jwtUtil.getMemberId(token);
        Member member = memberRepository.findById(memberId).orElseThrow();

        member.setFcmToken(body.get("fcmToken"));
        memberRepository.save(member);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Device registered for notifications"
        ));
    }

    @PostMapping("/unregister")
    public ResponseEntity<?> unregisterDevice(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        Long memberId = jwtUtil.getMemberId(token);
        Member member = memberRepository.findById(memberId).orElseThrow();

        member.setFcmToken(null);
        memberRepository.save(member);

        return ResponseEntity.ok(Map.of("success", true));
    }
}