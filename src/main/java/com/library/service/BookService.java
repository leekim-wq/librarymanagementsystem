package com.library.service;

import com.library.model.Book;
import com.library.model.Loan;
import com.library.model.Member;
import com.library.repository.BookRepository;
import com.library.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private AIService aiService;

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

    public void deleteBook(Long id) {
        bookRepository.deleteById(id);
    }

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

    public List<Book> getAIRecommendations(String query) {
        List<Book> availableBooks = getAvailableBooks();
        return aiService.getAIRecommendations(query, availableBooks);
    }

    public List<Book> getMostBorrowedBooks() {
        return bookRepository.findMostBorrowedBooks();
    }

    @Transactional
    public boolean borrowBook(Long bookId, Member member) {
        Optional<Book> bookOpt = bookRepository.findById(bookId);
        if (bookOpt.isEmpty()) return false;

        Book book = bookOpt.get();
        if (book.getAvailableQuantity() <= 0) return false;

        // Check if member can borrow
        if (!member.canBorrow()) return false;

        // Check if member already borrowed this book
        Optional<Loan> existingLoan = loanRepository.findByBookIdAndMemberIdAndReturnedFalse(bookId, member.getId());
        if (existingLoan.isPresent()) return false;

        // Create loan
        Loan loan = new Loan();
        loan.setBook(book);
        loan.setMember(member);
        loan.setBorrowDate(LocalDate.now());
        loan.setDueDate(LocalDate.now().plusDays(14));
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
        if (loanOpt.isEmpty()) return false;

        Loan loan = loanOpt.get();
        if (loan.isReturned()) return false;

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

        return true;
    }
}