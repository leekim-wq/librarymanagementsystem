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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock private BookRepository bookRepository;
    @Mock private LoanRepository loanRepository;
    @Mock private MemberRepository memberRepository;
    @Mock private AIService aiService;

    @InjectMocks
    private BookService bookService;

    private Book availableBook;
    private Book unavailableBook;
    private Member member;

    @BeforeEach
    void setUp() {
        availableBook = new Book();
        availableBook.setId(1L);
        availableBook.setTitle("Clean Code");
        availableBook.setAuthor("Robert Martin");
        availableBook.setQuantity(5);
        availableBook.setAvailableQuantity(5);
        availableBook.setTotalBorrows(0);
        availableBook.setActive(true);

        unavailableBook = new Book();
        unavailableBook.setId(2L);
        unavailableBook.setTitle("The Pragmatic Programmer");
        unavailableBook.setQuantity(5);
        unavailableBook.setAvailableQuantity(0);
        unavailableBook.setActive(true);

        member = new Member();
        member.setId(3L);
        member.setName("Test Member");
        member.setBorrowingLimit(5);
        member.setTotalFines(0.0);
        member.setActive(true);
    }

    // ============================================================
    // BORROW BOOK TESTS
    // ============================================================

    @Test
    void borrowBook_shouldSucceed_whenBookAvailableAndMemberEligible() {
        // Arrange — use findActiveById (which is what the service actually calls)
        when(bookRepository.findActiveById(1L)).thenReturn(Optional.of(availableBook));
        when(memberRepository.findById(3L)).thenReturn(Optional.of(member));
        when(loanRepository.countByMemberAndReturnedFalse(member)).thenReturn(0L);
        when(loanRepository.findByBookIdAndMemberIdAndReturnedFalse(1L, 3L))
                .thenReturn(Optional.empty());
        when(loanRepository.save(any(Loan.class))).thenAnswer(i -> i.getArgument(0));
        when(bookRepository.save(any(Book.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        boolean result = bookService.borrowBook(1L, member);

        // Assert
        assertTrue(result, "Borrow should succeed for available book");
        assertEquals(4, availableBook.getAvailableQuantity(),
                "Availability should decrement by 1");
        assertEquals(1, availableBook.getTotalBorrows(),
                "Total borrows should increment by 1");
        verify(loanRepository, times(1)).save(any(Loan.class));
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void borrowBook_shouldFail_whenBookNotFound() {
        when(bookRepository.findActiveById(999L)).thenReturn(Optional.empty());

        boolean result = bookService.borrowBook(999L, member);

        assertFalse(result);
        verify(loanRepository, never()).save(any());
    }

    @Test
    void borrowBook_shouldFail_whenNoAvailableCopies() {
        when(bookRepository.findActiveById(2L)).thenReturn(Optional.of(unavailableBook));

        boolean result = bookService.borrowBook(2L, member);

        assertFalse(result, "Should not borrow when no copies available");
        verify(loanRepository, never()).save(any());
    }

    @Test
    void borrowBook_shouldFail_whenMemberAtLimit() {
        Member atLimitMember = new Member();
        atLimitMember.setId(3L);
        atLimitMember.setBorrowingLimit(2);
        atLimitMember.setTotalFines(0.0);

        when(bookRepository.findActiveById(1L)).thenReturn(Optional.of(availableBook));
        when(memberRepository.findById(3L)).thenReturn(Optional.of(atLimitMember));
        when(loanRepository.countByMemberAndReturnedFalse(atLimitMember)).thenReturn(2L);

        boolean result = bookService.borrowBook(1L, atLimitMember);

        assertFalse(result, "Should fail when member at borrowing limit");
        verify(loanRepository, never()).save(any());
    }

    @Test
    void borrowBook_shouldFail_whenMemberHasUnpaidFines() {
        Member finedMember = new Member();
        finedMember.setId(3L);
        finedMember.setBorrowingLimit(5);
        finedMember.setTotalFines(150.0); // over KES 100 threshold

        when(bookRepository.findActiveById(1L)).thenReturn(Optional.of(availableBook));
        when(memberRepository.findById(3L)).thenReturn(Optional.of(finedMember));
        when(loanRepository.countByMemberAndReturnedFalse(finedMember)).thenReturn(0L);

        boolean result = bookService.borrowBook(1L, finedMember);

        assertFalse(result, "Should fail when member has excessive fines");
        verify(loanRepository, never()).save(any());
    }

    @Test
    void borrowBook_shouldFail_whenAlreadyBorrowed() {
        Loan existingLoan = new Loan();
        existingLoan.setId(99L);

        when(bookRepository.findActiveById(1L)).thenReturn(Optional.of(availableBook));
        when(memberRepository.findById(3L)).thenReturn(Optional.of(member));
        when(loanRepository.countByMemberAndReturnedFalse(member)).thenReturn(0L);
        when(loanRepository.findByBookIdAndMemberIdAndReturnedFalse(1L, 3L))
                .thenReturn(Optional.of(existingLoan));

        boolean result = bookService.borrowBook(1L, member);

        assertFalse(result, "Should fail when member already borrowed this book");
        verify(loanRepository, never()).save(any());
    }

    @Test
    void borrowBook_shouldFail_whenBookIdIsNull() {
        boolean result = bookService.borrowBook(null, member);
        assertFalse(result);
        verify(bookRepository, never()).findActiveById(anyLong());
    }

    @Test
    void borrowBook_shouldFail_whenMemberIsNull() {
        boolean result = bookService.borrowBook(1L, null);
        assertFalse(result);
        verify(bookRepository, never()).findActiveById(anyLong());
    }

    // ============================================================
    // SOFT DELETE TESTS
    // ============================================================

    @Test
    void deleteBook_shouldMarkInactive() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(availableBook));
        when(bookRepository.save(any(Book.class))).thenAnswer(i -> i.getArgument(0));

        boolean result = bookService.deleteBook(1L);

        assertTrue(result);
        assertFalse(availableBook.isActive(), "Book should be marked inactive");
        verify(bookRepository, times(1)).save(availableBook);
    }

    @Test
    void deleteBook_shouldReturnFalse_whenBookNotFound() {
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        boolean result = bookService.deleteBook(999L);

        assertFalse(result);
    }

    @Test
    void deleteBook_shouldReturnFalse_whenAlreadyInactive() {
        availableBook.setActive(false);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(availableBook));

        boolean result = bookService.deleteBook(1L);

        assertFalse(result);
        verify(bookRepository, never()).save(any());
    }

    @Test
    void restoreBook_shouldMarkActive() {
        availableBook.setActive(false);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(availableBook));
        when(bookRepository.save(any(Book.class))).thenAnswer(i -> i.getArgument(0));

        boolean result = bookService.restoreBook(1L);

        assertTrue(result);
        assertTrue(availableBook.isActive());
    }

    // ============================================================
    // SEARCH TESTS
    // ============================================================

    @Test
    void searchBooks_shouldReturnAll_whenQueryEmpty() {
        when(bookRepository.findAllActive())
                .thenReturn(java.util.List.of(availableBook));

        var result = bookService.searchBooks("");

        assertEquals(1, result.size());
        verify(bookRepository).findAllActive();
    }

    @Test
    void searchBooks_shouldCallRepository_whenQueryProvided() {
        when(bookRepository.searchActiveAcrossAllFields("python"))
                .thenReturn(java.util.List.of(availableBook));

        var result = bookService.searchBooks("python");

        assertEquals(1, result.size());
        verify(bookRepository).searchActiveAcrossAllFields("python");
    }
}