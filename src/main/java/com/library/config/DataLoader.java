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
            admin.setUsername("admin"); // ✅ ADDED: Set username
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
            librarian.setUsername("librarian"); // ✅ ADDED: Set username
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
            member.setUsername("member"); // ✅ ADDED: Set username
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

            // ✅ ADDED: Sample books for testing
            List<Book> books = Arrays.asList(
                    createBook("The Great Gatsby", "F. Scott Fitzgerald", "Classic", 5),
                    createBook("To Kill a Mockingbird", "Harper Lee", "Classic", 3),
                    createBook("1984", "George Orwell", "Dystopian", 4),
                    createBook("Pride and Prejudice", "Jane Austen", "Romance", 3),
                    createBook("The Catcher in the Rye", "J.D. Salinger", "Classic", 2)
            );

            bookRepository.saveAll(books);
            System.out.println("✅ " + books.size() + " books loaded successfully!");
        }
    }

    // Helper method to create books
    private Book createBook(String title, String author, String category, int quantity) {
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setCategory(category);
        book.setQuantity(quantity);
        book.setAvailableQuantity(quantity);
        book.setTotalBorrows(0);
        book.setRating(0.0);
        return book;
    }
}