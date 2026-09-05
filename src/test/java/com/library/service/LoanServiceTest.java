package com.library.service;

import com.library.model.Book;
import com.library.model.Loan;
import com.library.model.Member;
import com.library.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @InjectMocks
    private LoanService loanService;

    private Loan loan;
    private Member member;
    private Book book;

    @BeforeEach
    void setUp() {
        member = new Member();
        member.setId(1L);
        member.setEmail("member@test.com");

        book = new Book();
        book.setId(1L);
        book.setTitle("Test Book");

        loan = new Loan();
        loan.setId(1L);
        loan.setBook(book);
        loan.setMember(member);
        loan.setBorrowDate(LocalDate.now());
        loan.setDueDate(LocalDate.now().plusDays(14));
        loan.setReturned(false);
        loan.setFine(0.0);
    }

    // ---------- Basic CRUD ----------

    @Test
    void getAllLoans_shouldReturnAllLoans() {
        when(loanRepository.findAll()).thenReturn(List.of(loan));

        List<Loan> result = loanService.getAllLoans();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(loanRepository, times(1)).findAll();
    }

    @Test
    void getLoanById_shouldReturnLoan() {
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

        Optional<Loan> result = loanService.getLoanById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        verify(loanRepository, times(1)).findById(1L);
    }

    @Test
    void saveLoan_shouldSaveAndReturnLoan() {
        when(loanRepository.save(any(Loan.class))).thenReturn(loan);

        Loan saved = loanService.saveLoan(loan);

        assertNotNull(saved);
        assertEquals(1L, saved.getId());
        verify(loanRepository, times(1)).save(loan);
    }

    @Test
    void deleteLoan_shouldDeleteLoan() {
        doNothing().when(loanRepository).deleteById(1L);

        loanService.deleteLoan(1L);

        verify(loanRepository, times(1)).deleteById(1L);
    }

    // ---------- Member-specific (using eager methods) ----------

    @Test
    void getAllLoansForMember_shouldReturnAllLoansForMember() {
        // ✅ Mock the eager method used by LoanService
        when(loanRepository.findByMemberWithBook(any(Member.class))).thenReturn(List.of(loan));

        List<Loan> result = loanService.getAllLoansForMember(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(loanRepository, times(1)).findByMemberWithBook(any(Member.class));
    }

    @Test
    void getActiveLoansForMember_shouldReturnActiveLoansForMember() {
        // ✅ Mock the eager method used by LoanService
        when(loanRepository.findByMemberAndReturnedFalseWithBook(any(Member.class))).thenReturn(List.of(loan));

        List<Loan> result = loanService.getActiveLoansForMember(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(loanRepository, times(1)).findByMemberAndReturnedFalseWithBook(any(Member.class));
    }

    @Test
    void isLoanBelongsToMember_shouldReturnTrue_whenLoanBelongsToMember() {
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

        boolean result = loanService.isLoanBelongsToMember(1L, 1L);

        assertTrue(result);
        verify(loanRepository, times(1)).findById(1L);
    }

    @Test
    void isLoanBelongsToMember_shouldReturnFalse_whenLoanDoesNotBelongToMember() {
        Member otherMember = new Member();
        otherMember.setId(2L);
        loan.setMember(otherMember);

        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

        boolean result = loanService.isLoanBelongsToMember(1L, 1L);

        assertFalse(result);
        verify(loanRepository, times(1)).findById(1L);
    }

    // ---------- Overdue and counting ----------

    @Test
    void getOverdueLoans_shouldReturnOverdueLoans() {
        when(loanRepository.findOverdueLoans()).thenReturn(List.of(loan));

        List<Loan> result = loanService.getOverdueLoans();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(loanRepository, times(1)).findOverdueLoans();
    }

    @Test
    void countActiveLoansByMember_shouldReturnCount() {
        // This method uses findByMemberAndReturnedFalse (not the eager one)
        when(loanRepository.findByMemberAndReturnedFalse(any(Member.class))).thenReturn(List.of(loan));

        long count = loanService.countActiveLoansByMember(1L);

        assertEquals(1, count);
        verify(loanRepository, times(1)).findByMemberAndReturnedFalse(any(Member.class));
    }

    // ---------- Deprecated methods (delegate to eager) ----------

    @Test
    @SuppressWarnings("deprecation")
    void getMemberLoans_shouldDelegateToActiveLoans() {
        // This calls getActiveLoansForMember → uses findByMemberAndReturnedFalseWithBook
        when(loanRepository.findByMemberAndReturnedFalseWithBook(any(Member.class))).thenReturn(List.of(loan));

        List<Loan> result = loanService.getMemberLoans(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(loanRepository, times(1)).findByMemberAndReturnedFalseWithBook(any(Member.class));
    }

    @Test
    @SuppressWarnings("deprecation")
    void getMemberAllLoans_shouldDelegateToAllLoans() {
        // This calls getAllLoansForMember → uses findByMemberWithBook
        when(loanRepository.findByMemberWithBook(any(Member.class))).thenReturn(List.of(loan));

        List<Loan> result = loanService.getMemberAllLoans(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(loanRepository, times(1)).findByMemberWithBook(any(Member.class));
    }
}