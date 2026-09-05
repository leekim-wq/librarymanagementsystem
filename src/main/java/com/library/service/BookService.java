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

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AIService aiService;

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

    // ===== NEW: Copy counts =====
    public long getTotalCopies() {
        return bookRepository.sumTotalCopies();
    }

    public long getAvailableCopies() {
        return bookRepository.sumAvailableCopies();
    }

    // ---------- Most borrowed ----------

    public List<Book> getMostBorrowedBooks() {
        return bookRepository.findMostBorrowedBooks();
    }

    // ---------- AI Recommendations ----------

    public List<Book> getAIRecommendations(String query) {
        List<Book> availableBooks = getAvailableBooks();
        if (availableBooks.isEmpty()) {
            return List.of();
        }
        try {
            return aiService.getAIRecommendations(query, availableBooks);
        } catch (Exception e) {
            log.warn("AI recommendation failed, returning top 5 available books", e);
            return availableBooks.stream().limit(5).collect(Collectors.toList());
        }
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
            // 1. Fetch book
            Optional<Book> bookOpt = bookRepository.findById(bookId);
            if (bookOpt.isEmpty()) {
                log.warn("Book not found with id: {}", bookId);
                return false;
            }
            Book book = bookOpt.get();
            log.debug("Book found: title={}, availableQuantity={}", book.getTitle(), book.getAvailableQuantity());

            // 2. Check availability
            if (book.getAvailableQuantity() <= 0) {
                log.warn("Book not available: availableQuantity={}", book.getAvailableQuantity());
                return false;
            }

            // 3. Fetch fresh member
            Optional<Member> memberOpt = memberRepository.findById(member.getId());
            if (memberOpt.isEmpty()) {
                log.warn("Member not found with id: {}", member.getId());
                return false;
            }
            Member attachedMember = memberOpt.get();
            log.debug("Member found: email={}, borrowingLimit={}, totalFines={}",
                    attachedMember.getEmail(), attachedMember.getBorrowingLimit(), attachedMember.getTotalFines());

            // 4. Check borrowing eligibility
            if (!attachedMember.canBorrow()) {
                log.warn("Member cannot borrow: loans count or fines exceeded");
                long activeLoans = loanRepository.countByMemberAndReturnedFalse(attachedMember);
                log.debug("Active loans count: {}", activeLoans);
                return false;
            }

            // 5. Check if already borrowed
            Optional<Loan> existingLoan = loanRepository.findByBookIdAndMemberIdAndReturnedFalse(
                    bookId, attachedMember.getId());
            if (existingLoan.isPresent()) {
                log.warn("Member already has an active loan for this book");
                return false;
            }

            // 6. Create loan
            Loan loan = new Loan();
            loan.setBook(book);
            loan.setMember(attachedMember);
            loan.setBorrowDate(LocalDate.now());
            loan.setDueDate(LocalDate.now().plusDays(14));

            loanRepository.save(loan);
            log.debug("Loan created with id: {}", loan.getId());

            // 7. Update book availability
            book.setAvailableQuantity(book.getAvailableQuantity() - 1);
            book.setTotalBorrows(book.getTotalBorrows() + 1);
            bookRepository.save(book);
            log.debug("Book availability updated to: {}", book.getAvailableQuantity());

            return true;

        } catch (Exception e) {
            log.error("Unexpected error during borrowBook for bookId={}, memberId={}: {}",
                    bookId, member.getId(), e.getMessage(), e);
            return false;
        }
    }

    @Transactional
    public boolean returnBook(Long loanId) {
        if (loanId == null) {
            log.warn("ReturnBook called with null loanId");
            return false;
        }

        try {
            Optional<Loan> loanOpt = loanRepository.findById(loanId);
            if (loanOpt.isEmpty()) {
                log.warn("Loan not found with id: {}", loanId);
                return false;
            }

            Loan loan = loanOpt.get();

            if (loan.isReturned()) {
                log.warn("Loan already returned: {}", loanId);
                return false;
            }

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

            log.debug("Book returned successfully, fine={}", fine);
            return true;

        } catch (Exception e) {
            log.error("Unexpected error during returnBook for loanId={}: {}", loanId, e.getMessage(), e);
            return false;
        }
    }

    // ---------- Utility ----------

    public boolean isBookAvailable(Long bookId) {
        Optional<Book> bookOpt = bookRepository.findById(bookId);
        return bookOpt.map(Book::isAvailable).orElse(false);
    }

    public long getActiveLoansCount(Member member) {
        if (member == null || member.getId() == null) return 0;
        return loanRepository.countByMemberAndReturnedFalse(member);
    }
}