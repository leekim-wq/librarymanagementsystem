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

    // ---------- Basic CRUD ----------

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

    // ---------- Search & Filters ----------

    /**
     * Multi-field search across title, author, category, and description.
     * Results are sorted by rating (best first).
     * Returns an empty list if nothing matches.
     */
    public List<Book> searchBooks(String query) {
        if (query == null || query.trim().isEmpty()) {
            return bookRepository.findAll();
        }
        String trimmed = query.trim();
        List<Book> results = bookRepository.searchAcrossAllFields(trimmed);
        results.sort((a, b) -> {
            double r1 = a.getRating() == null ? 0.0 : a.getRating();
            double r2 = b.getRating() == null ? 0.0 : b.getRating();
            return Double.compare(r2, r1);
        });
        return results;
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
                .filter(c -> c != null && !c.isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    // ---------- Copy counts ----------

    public long getTotalCopies() {
        Long v = bookRepository.sumTotalCopies();
        return v == null ? 0L : v;
    }

    public long getAvailableCopies() {
        Long v = bookRepository.sumAvailableCopies();
        return v == null ? 0L : v;
    }

    // ---------- Most borrowed ----------

    public List<Book> getMostBorrowedBooks() {
        return bookRepository.findMostBorrowedBooks();
    }

    // ---------- AI Recommendations ----------

    /**
     * Returns only books that actually match the query.
     * If nothing matches, returns an EMPTY list (no random padding).
     */
    public List<Book> getAIRecommendations(String query) {
        List<Book> availableBooks = getAvailableBooks();
        if (availableBooks.isEmpty()) {
            return List.of();
        }
        // Do NOT catch and pad with random books — return what AI returns
        return aiService.getAIRecommendations(query, availableBooks);
    }

    // ---------- Borrowing Eligibility ----------

    public boolean canBorrow(Member member) {
        if (member == null || member.getId() == null) return false;
        long activeLoans = loanRepository.countByMemberAndReturnedFalse(member);
        int limit = member.getBorrowingLimit() == null ? 5 : member.getBorrowingLimit();
        return activeLoans < limit && member.getTotalFines() < FINE_LIMIT;
    }

    // ---------- Borrow / Return ----------

    @PreAuthorize("hasRole('MEMBER')")
    @Transactional
    public boolean borrowBook(Long bookId, Member member) {
        if (bookId == null || member == null || member.getId() == null) {
            log.warn("Invalid borrow request: bookId={}, member={}", bookId, member);
            return false;
        }
        log.debug("Attempting to borrow bookId={} for memberId={}", bookId, member.getId());

        try {
            Optional<Book> bookOpt = bookRepository.findById(bookId);
            if (bookOpt.isEmpty()) {
                log.warn("Book not found: {}", bookId);
                return false;
            }
            Book book = bookOpt.get();

            if (book.getAvailableQuantity() <= 0) {
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
            book.setTotalBorrows(book.getTotalBorrows() + 1);
            bookRepository.save(book);

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
            book.setAvailableQuantity(book.getAvailableQuantity() + 1);
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

    // ---------- Utility ----------

    public boolean isBookAvailable(Long bookId) {
        return bookRepository.findById(bookId).map(Book::isAvailable).orElse(false);
    }

    public long getActiveLoansCount(Member member) {
        if (member == null || member.getId() == null) return 0;
        return loanRepository.countByMemberAndReturnedFalse(member);
    }
}