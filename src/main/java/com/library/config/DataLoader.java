package com.library.config;

import com.library.model.Book;
import com.library.model.Member;
import com.library.repository.BookRepository;
import com.library.repository.MemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Slf4j
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
    public void run(String... args) {
        try {
            loadMembers();
            loadBooks();
        } catch (Exception e) {
            log.error("Error loading data: {}", e.getMessage());
            // Don't throw - allow application to start
        }
    }

    private void loadMembers() {
        try {
            if (memberRepository.count() > 0) {
                log.info("✅ Members already exist, skipping creation");
                return;
            }

            log.info("📚 Creating default members...");

            // Admin
            Member admin = new Member();
            admin.setEmail("admin@library.com");
            admin.setUsername("admin");
            admin.setName("System Administrator");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("ADMIN");
            admin.setActive(true);
            admin.setMembershipDate(LocalDate.now());
            admin.setMembershipType("Premium");
            admin.setBorrowingLimit(10);
            admin.setTotalFines(0.0);
            memberRepository.save(admin);
            log.info("✅ Admin user created! (email=admin@library.com, username=admin)");

            // Librarian
            Member librarian = new Member();
            librarian.setEmail("librarian@library.com");
            librarian.setUsername("librarian");
            librarian.setName("Head Librarian");
            librarian.setPassword(passwordEncoder.encode("lib123"));
            librarian.setRole("LIBRARIAN");
            librarian.setActive(true);
            librarian.setMembershipDate(LocalDate.now());
            librarian.setMembershipType("Premium");
            librarian.setBorrowingLimit(15);
            librarian.setTotalFines(0.0);
            memberRepository.save(librarian);
            log.info("✅ Librarian user created! (email=librarian@library.com, username=librarian)");

            // Regular Member
            Member member = new Member();
            member.setEmail("member@library.com");
            member.setUsername("member");
            member.setName("Regular Member");
            member.setPassword(passwordEncoder.encode("member123"));
            member.setRole("MEMBER");
            member.setActive(true);
            member.setMembershipDate(LocalDate.now());
            member.setMembershipType("Standard");
            member.setBorrowingLimit(5);
            member.setTotalFines(0.0);
            memberRepository.save(member);
            log.info("✅ Member user created! (email=member@library.com, username=member)");

            log.info("✅ All members created successfully!");
        } catch (Exception e) {
            log.warn("Could not load members: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadBooks() {
        try {
            if (bookRepository.count() > 0) {
                log.info("✅ Books already exist, skipping creation");
                return;
            }

            log.info("📚 Loading sample books...");

            List<Book> books = Arrays.asList(
                    createBook("The Great Gatsby", "F. Scott Fitzgerald", "Classic", 5),
                    createBook("To Kill a Mockingbird", "Harper Lee", "Classic", 3),
                    createBook("1984", "George Orwell", "Dystopian", 4),
                    createBook("Pride and Prejudice", "Jane Austen", "Romance", 3),
                    createBook("The Catcher in the Rye", "J.D. Salinger", "Fiction", 2),
                    createBook("Harry Potter and the Sorcerer's Stone", "J.K. Rowling", "Fantasy", 6),
                    createBook("The Hobbit", "J.R.R. Tolkien", "Fantasy", 4),
                    createBook("The Da Vinci Code", "Dan Brown", "Mystery", 3)
            );

            bookRepository.saveAll(books);
            log.info("✅ {} books loaded successfully!", books.size());
        } catch (Exception e) {
            log.warn("Could not load books: {}", e.getMessage());
            e.printStackTrace();
        }
    }

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