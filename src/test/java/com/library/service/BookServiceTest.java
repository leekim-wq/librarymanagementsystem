package com.library.service;

import com.library.model.Book;
import com.library.model.Loan;
import com.library.model.Member;
import com.library.repository.BookRepository;
import com.library.repository.LoanRepository;
import com.library.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private AIService aiService;

    @InjectMocks
    private BookService bookService;

    private Book book;
    private Member member;
    private Loan loan;

    @BeforeEach
    void setUp() {
        book = new Book();
        book.setId(1L);
        book.setTitle("Test Book");
        book.setAuthor("Test Author");
        book.setQuantity(5);
        book.setAvailableQuantity(3);
        book.setTotalBorrows(0);

        member = new Member();
        member.setId(1L);
        member.setEmail("member@test.com");
        member.setBorrowingLimit(5);
        member.setTotalFines(0.0);
        member.setLoans(new ArrayList<>()); // empty list so canBorrow() returns true

        loan = new Loan();
        loan.setId(1L);
        loan.setBook(book);
        loan.setMember(member);
        loan.setBorrowDate(LocalDate.now());
        loan.setDueDate(LocalDate.now().plusDays(14));
        loan.setReturned(false);
        loan.setFine(0.0);
    }

    // ... other tests (getAllBooks, getBookById, saveBook, deleteBook, searchBooks, etc.)
    // They remain unchanged. I'll include only the corrected tests that had issues.

    // ===== Corrected: borrowBook_shouldSucceed_whenBookAvailableAndMemberEligible =====
    @Test
    void borrowBook_shouldSucceed_whenBookAvailableAndMemberEligible() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        // No need to stub countByMemberAndReturnedFalse because canBorrow() uses the in-memory loans list.
        when(loanRepository.findByBookIdAndMemberIdAndReturnedFalse(1L, 1L)).thenReturn(Optional.empty());
        when(loanRepository.save(any(Loan.class))).thenReturn(loan);
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        boolean result = bookService.borrowBook(1L, member);

        assertTrue(result);
        verify(loanRepository, times(1)).save(any(Loan.class));
        verify(bookRepository, times(1)).save(book);
        assertEquals(2, book.getAvailableQuantity());
        assertEquals(1, book.getTotalBorrows());
    }

    // ===== Corrected: borrowBook_shouldFail_whenAlreadyBorrowed =====
    @Test
    void borrowBook_shouldFail_whenAlreadyBorrowed() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        // canBorrow() uses in-memory loans (empty), so no need to stub count
        when(loanRepository.findByBookIdAndMemberIdAndReturnedFalse(1L, 1L))
                .thenReturn(Optional.of(loan)); // loan exists

        boolean result = bookService.borrowBook(1L, member);

        assertFalse(result);
        verify(loanRepository, never()).save(any(Loan.class));
        verify(bookRepository, never()).save(any(Book.class));
    }

    // ===== The rest of the tests remain exactly as before =====
    // ... (include all other test methods from the previously provided file)
}