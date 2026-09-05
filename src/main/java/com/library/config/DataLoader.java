package com.library.config;

import com.library.model.Book;
import com.library.model.Member;
import com.library.repository.BookRepository;
import com.library.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Create default users if they don't exist
        if (!memberRepository.existsByEmail("admin@library.com")) {
            Member admin = new Member();
            admin.setEmail("admin@library.com");
            admin.setName("System Administrator");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("ADMIN");
            admin.setActive(true);
            admin.setMembershipDate(LocalDate.now());
            memberRepository.save(admin);
            System.out.println("✅ Admin user created!");
        }

        if (!memberRepository.existsByEmail("librarian@library.com")) {
            Member librarian = new Member();
            librarian.setEmail("librarian@library.com");
            librarian.setName("Head Librarian");
            librarian.setPassword(passwordEncoder.encode("lib123"));
            librarian.setRole("LIBRARIAN");
            librarian.setActive(true);
            librarian.setMembershipDate(LocalDate.now());
            memberRepository.save(librarian);
            System.out.println("✅ Librarian user created!");
        }

        if (!memberRepository.existsByEmail("member@library.com")) {
            Member member = new Member();
            member.setEmail("member@library.com");
            member.setName("Regular Member");
            member.setPassword(passwordEncoder.encode("member123"));
            member.setRole("MEMBER");
            member.setActive(true);
            member.setMembershipDate(LocalDate.now());
            memberRepository.save(member);
            System.out.println("✅ Member user created!");
        }

        // Load books if empty
        if (bookRepository.count() == 0) {
            System.out.println("📚 Loading books...");
            // Your book loading code here
            System.out.println("✅ Books loaded successfully!");
        }
    }
}