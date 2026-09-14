package com.library.service;

import com.library.model.Book;
import com.library.model.Loan;
import com.library.model.Member;
import com.library.repository.BookRepository;
import com.library.repository.LoanRepository;
import com.library.repository.MemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookService {

    private static final Logger log = LoggerFactory.getLogger(BookService.class);
    private static final double FINE_LIMIT = 100.0;

    @Autowired private BookRepository bookRepository;
    @Autowired private LoanRepository loanRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private AIService aiService;

    // ============================================================
    // Basic CRUD — reads return only active books
    // ============================================================

    /** Returns only ACTIVE books (hides soft-deleted). */
    public List<Book> getAllBooks() {
        return bookRepository.findAllActive();
    }

    /** Returns only ACTIVE books. */
    public Optional<Book> getBookById(Long id) {
        if (id == null) return Optional.empty();
        return bookRepository.findActiveById(id);
    }

    /** Save or update a book. New books default to active=true. */
    public Book saveBook(Book book) {
        if (book.getAvailableQuantity() == null) {
            book.setAvailableQuantity(book.getQuantity() == null ? 1 : book.getQuantity());
        }
        if (book.getQuantity() == null) {
            book.setQuantity(1);
        }
        if (book.getTotalBorrows() == null) {
            book.setTotalBorrows(0);
        }
        if (book.getRating() == null) {
            book.setRating(0.0);
        }
        return bookRepository.save(book);
    }

    public void saveAll(List<Book> books) {
        bookRepository.saveAll(books);
    }

    // ============================================================
    // SOFT DELETE
    // Marks the book inactive instead of removing the row.
    // Preserves loan history and prevents foreign key errors.
    // ============================================================

    /**
     * Soft-delete a book: sets active=false.
     * The book disappears from the catalog but loan history stays intact.
     *
     * @return true if a book was found and marked inactive; false otherwise.
     */
    @Transactional
    public boolean deleteBook(Long id) {
        if (id == null) {
            log.warn("deleteBook called with null id");
            return false;
        }

        Optional<Book> bookOpt = bookRepository.findById(id);
        if (bookOpt.isEmpty()) {
            log.warn("deleteBook: book not found, id={}", id);
            return false;
        }

        Book book = bookOpt.get();
        if (!book.isActive()) {
            log.warn("deleteBook: book already inactive, id={}", id);
            return false;
        }

        book.setActive(false);
        bookRepository.save(book);

        log.info("✅ Soft-deleted book id={} title='{}'", id, book.getTitle());
        return true;
    }

    /**
     * Restore a soft-deleted book (optional admin feature).
     */
    @Transactional
    public boolean restoreBook(Long id) {
        if (id == null) return false;

        Optional<Book> bookOpt = bookRepository.findById(id);
        if (bookOpt.isEmpty()) return false;

        Book book = bookOpt.get();
        if (book.isActive()) return false;

        book.setActive(true);
        bookRepository.save(book);
        log.info("✅ Restored book id={} title='{}'", id, book.getTitle());
        return true;
    }

    // ============================================================
    // Search & Filters — ACTIVE books only
    // ============================================================

    public List<Book> searchBooks(String query) {
        if (query == null || query.trim().isEmpty()) {
            return bookRepository.findAllActive();
        }
        return bookRepository.searchActiveAcrossAllFields(query.trim());
    }

    public List<Book> getAvailableBooks() {
        return bookRepository.findAvailableBooks();
    }

    public List<Book> getBooksByCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return bookRepository.findAllActive();
        }
        return bookRepository.findActiveByCategory(category.trim());
    }

    public List<String> getAllCategories() {
        return bookRepository.findAllActiveCategories();
    }

    // ============================================================
    // Copy counts
    // ============================================================

    public long getTotalCopies() {
        Long v = bookRepository.sumTotalCopies();
        return v == null ? 0L : v;
    }

    public long getAvailableCopies() {
        Long v = bookRepository.sumAvailableCopies();
        return v == null ? 0L : v;
    }

    // ============================================================
    // Most borrowed
    // ============================================================

    public List<Book> getMostBorrowedBooks() {
        return bookRepository.findMostBorrowedBooks();
    }

    // ============================================================
    // AI Recommendations
    // ============================================================

    public List<Book> getAIRecommendations(String query) {
        List<Book> availableBooks = getAvailableBooks();
        if (availableBooks.isEmpty()) {
            return List.of();
        }
        return aiService.getAIRecommendations(query, availableBooks);
    }

    // ============================================================
    // Borrowing eligibility
    // ============================================================

    public boolean canBorrow(Member member) {
        if (member == null || member.getId() == null) return false;
        long activeLoans = loanRepository.countByMemberAndReturnedFalse(member);
        int limit = member.getBorrowingLimit() == null ? 5 : member.getBorrowingLimit();
        return activeLoans < limit && member.getTotalFines() < FINE_LIMIT;
    }

    // ============================================================
    // Borrow / Return
    // ============================================================

    @PreAuthorize("hasRole('MEMBER')")
    @Transactional
    public boolean borrowBook(Long bookId, Member member) {
        if (bookId == null || member == null || member.getId() == null) {
            log.warn("Invalid borrow request: bookId={}, member={}", bookId, member);
            return false;
        }

        try {
            Optional<Book> bookOpt = bookRepository.findActiveById(bookId);
            if (bookOpt.isEmpty()) {
                log.warn("Book not found or inactive: {}", bookId);
                return false;
            }
            Book book = bookOpt.get();

            if (book.getAvailableQuantity() == null || book.getAvailableQuantity() <= 0) {
                log.warn("Book not available: {}", bookId);
                return false;
            }

            Optional<Member> memberOpt = memberRepository.findById(member.getId());
            if (memberOpt.isEmpty()) {
                log.warn("Member not found: {}", member.getId());
                return false;
            }
            Member attached = memberOpt.get();

            if (!canBorrow(attached)) {
                log.warn("Member cannot borrow: id={}", attached.getId());
                return false;
            }

            Optional<Loan> existing = loanRepository
                    .findByBookIdAndMemberIdAndReturnedFalse(bookId, attached.getId());
            if (existing.isPresent()) {
                log.warn("Member already borrowed this book");
                return false;
            }

            Loan loan = new Loan();
            loan.setBook(book);
            loan.setMember(attached);
            loan.setBorrowDate(LocalDate.now());
            loan.setDueDate(LocalDate.now().plusDays(14));
            loanRepository.save(loan);

            book.setAvailableQuantity(book.getAvailableQuantity() - 1);
            book.setTotalBorrows((book.getTotalBorrows() == null ? 0 : book.getTotalBorrows()) + 1);
            bookRepository.save(book);

            log.info("✅ Book borrowed: bookId={}, memberId={}", bookId, member.getId());
            return true;

        } catch (Exception e) {
            log.error("Error during borrowBook: {}", e.getMessage(), e);
            return false;
        }
    }

    @Transactional
    public boolean returnBook(Long loanId) {
        if (loanId == null) return false;

        try {
            Optional<Loan> loanOpt = loanRepository.findById(loanId);
            if (loanOpt.isEmpty()) return false;

            Loan loan = loanOpt.get();
            if (loan.isReturned()) return false;

            loan.setReturned(true);
            loan.setReturnDate(LocalDate.now());
            double fine = loan.calculateFine();
            loan.setFine(fine);
            loanRepository.save(loan);

            Book book = loan.getBook();
            book.setAvailableQuantity((book.getAvailableQuantity() == null ? 0 : book.getAvailableQuantity()) + 1);
            bookRepository.save(book);

            Member member = loan.getMember();
            member.setTotalFines(member.getTotalFines() + fine);
            memberRepository.save(member);

            return true;

        } catch (Exception e) {
            log.error("Error during returnBook: {}", e.getMessage(), e);
            return false;
        }
    }

    // ============================================================
    // Utility
    // ============================================================

    public boolean isBookAvailable(Long bookId) {
        return bookRepository.findActiveById(bookId).map(Book::isAvailable).orElse(false);
    }

    public long getActiveLoansCount(Member member) {
        if (member == null || member.getId() == null) return 0;
        return loanRepository.countByMemberAndReturnedFalse(member);
    }
}