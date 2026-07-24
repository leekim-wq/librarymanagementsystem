package com.library.service;

import com.library.model.Book;
import com.library.model.Loan;
import com.library.model.Member;
import com.library.repository.BookRepository;
import com.library.repository.LoanRepository;
import com.library.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AIService aiService;

    // ========== BASIC CRUD ==========

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Optional<Book> getBookById(Long id) {
        return bookRepository.findById(id);
    }

    public Book saveBook(Book book) {
        if (book.getAvailableQuantity() == null) {
            book.setAvailableQuantity(book.getQuantity());
        }
        return bookRepository.save(book);
    }

    public void saveAll(List<Book> books) {
        bookRepository.saveAll(books);
    }

    public void deleteBook(Long id) {
        bookRepository.deleteById(id);
    }

    // ========== SEARCH ==========

    public List<Book> searchBooks(String query) {
        if (query == null || query.trim().isEmpty()) {
            return bookRepository.findAll();
        }
        return bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(
                query.trim(), query.trim());
    }

    public List<Book> getAvailableBooks() {
        return bookRepository.findAvailableBooks();
    }

    public List<Book> getBooksByCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return bookRepository.findAll();
        }
        return bookRepository.findByCategory(category.trim());
    }

    public List<String> getAllCategories() {
        return bookRepository.findAll().stream()
                .map(Book::getCategory)
                .filter(category -> category != null && !category.isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public List<Book> getMostBorrowedBooks() {
        return bookRepository.findMostBorrowedBooks();
    }

    // ========== AI RECOMMENDATIONS ==========

    public List<Book> getAIRecommendations(String query) {
        List<Book> availableBooks = getAvailableBooks();
        if (availableBooks.isEmpty()) {
            return List.of();
        }
        try {
            return aiService.getAIRecommendations(query, availableBooks);
        } catch (Exception e) {
            return availableBooks.stream().limit(5).collect(Collectors.toList());
        }
    }

    // ========== BORROWING & RETURN - FIXED ==========

    @Transactional
    public boolean borrowBook(Long bookId, Member member) {
        // Fetch fresh member from database with proper transaction
        Member freshMember = memberRepository.findById(member.getId())
                .orElseThrow(() -> new RuntimeException("Member not found"));

        // Check if book exists
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        // Check if book is available
        if (book.getAvailableQuantity() <= 0) {
            return false;
        }

        // Check borrowing limit using repository query instead of lazy loading
        long activeLoans = loanRepository.countByMemberAndReturnedFalse(freshMember);

        // Check if member can borrow (not exceeding limit and fines < 100)
        if (activeLoans >= freshMember.getBorrowingLimit()) {
            return false;
        }

        if (freshMember.getTotalFines() >= 100.0) {
            return false;
        }

        // Check if member already borrowed this book
        Optional<Loan> existingLoan = loanRepository.findByBookIdAndMemberIdAndReturnedFalse(
                bookId, freshMember.getId());
        if (existingLoan.isPresent()) {
            return false;
        }

        // Create new loan
        Loan loan = new Loan();
        loan.setBook(book);
        loan.setMember(freshMember);
        loan.setBorrowDate(LocalDate.now());
        loan.setDueDate(LocalDate.now().plusDays(14)); // 2 weeks

        // Save loan
        loanRepository.save(loan);

        // Update book quantity
        book.setAvailableQuantity(book.getAvailableQuantity() - 1);
        book.setTotalBorrows(book.getTotalBorrows() + 1);
        bookRepository.save(book);

        return true;
    }

    @Transactional
    public boolean returnBook(Long loanId) {
        Optional<Loan> loanOpt = loanRepository.findById(loanId);
        if (loanOpt.isEmpty()) {
            return false;
        }

        Loan loan = loanOpt.get();

        if (loan.isReturned()) {
            return false;
        }

        // Mark as returned
        loan.setReturned(true);
        loan.setReturnDate(LocalDate.now());

        // Calculate fine if overdue
        double fine = loan.calculateFine();
        loan.setFine(fine);
        loanRepository.save(loan);

        // Update book quantity
        Book book = loan.getBook();
        book.setAvailableQuantity(book.getAvailableQuantity() + 1);
        bookRepository.save(book);

        // Update member fines
        Member member = loan.getMember();
        member.setTotalFines(member.getTotalFines() + fine);
        memberRepository.save(member);

        return true;
    }

    public boolean isBookAvailable(Long bookId) {
        Optional<Book> bookOpt = bookRepository.findById(bookId);
        if (bookOpt.isPresent()) {
            Book book = bookOpt.get();
            return book.isAvailable();
        }
        return false;
    }
}