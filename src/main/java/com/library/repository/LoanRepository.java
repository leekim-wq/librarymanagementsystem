package com.library.repository;

import com.library.model.Book;
import com.library.model.Loan;
import com.library.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    // Find all active loans for a member
    List<Loan> findByMemberAndReturnedFalse(Member member);

    // Find all loans for a member (including returned)
    List<Loan> findByMember(Member member);

    // Find all active loans for a book
    List<Loan> findByBookAndReturnedFalse(Book book);

    // Find a specific active loan by book and member
    Optional<Loan> findByBookIdAndMemberIdAndReturnedFalse(Long bookId, Long memberId);

    // Find all overdue loans
    @Query("SELECT l FROM Loan l WHERE l.returned = false AND l.dueDate < CURRENT_DATE")
    List<Loan> findOverdueLoans();

    // Find overdue loans for a specific member
    @Query("SELECT l FROM Loan l WHERE l.member = :member AND l.returned = false AND l.dueDate < CURRENT_DATE")
    List<Loan> findOverdueLoansByMember(@Param("member") Member member);

    // Count active loans for a member
    long countByMemberAndReturnedFalse(Member member);

    // Count active loans for a book
    long countByBookAndReturnedFalse(Book book);

    // Find loans by due date range
    List<Loan> findByDueDateBetweenAndReturnedFalse(LocalDate startDate, LocalDate endDate);

    // Find loans returned after a specific date
    List<Loan> findByReturnDateAfterAndReturnedTrue(LocalDate date);

    // Get total fines for a member
    @Query("SELECT SUM(l.fine) FROM Loan l WHERE l.member = :member")
    Double getTotalFinesByMember(@Param("member") Member member);

    // Get most borrowed books (with count)
    @Query("SELECT l.book, COUNT(l) as borrowCount FROM Loan l GROUP BY l.book ORDER BY borrowCount DESC")
    List<Object[]> findMostBorrowedBooksWithCount();
}