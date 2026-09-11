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

    // ---------- Member-scoped queries ----------

    List<Loan> findByMember(Member member);

    List<Loan> findByMemberAndReturnedFalse(Member member);

    @Query("SELECT l FROM Loan l WHERE l.member.id = :memberId AND l.returned = false")
    List<Loan> findByMemberIdAndReturnedFalse(@Param("memberId") Long memberId);

    long countByMemberAndReturnedFalse(Member member);

    long countByMember(Member member);

    // ---------- Book-scoped queries ----------

    List<Loan> findByBookAndReturnedFalse(Book book);

    long countByBookAndReturnedFalse(Book book);

    Optional<Loan> findByBookIdAndMemberIdAndReturnedFalse(Long bookId, Long memberId);

    // ---------- Status / Date queries ----------

    @Query("SELECT l FROM Loan l WHERE l.returned = false AND l.dueDate < CURRENT_DATE")
    List<Loan> findOverdueLoans();

    @Query("SELECT l FROM Loan l WHERE l.member = :member AND l.returned = false AND l.dueDate < CURRENT_DATE")
    List<Loan> findOverdueLoansByMember(@Param("member") Member member);

    List<Loan> findByDueDateBetweenAndReturnedFalse(LocalDate startDate, LocalDate endDate);

    List<Loan> findByReturnDateAfterAndReturnedTrue(LocalDate date);

    // ---------- Aggregates ----------

    @Query("SELECT COALESCE(SUM(l.fine), 0) FROM Loan l WHERE l.member = :member")
    Double getTotalFinesByMember(@Param("member") Member member);

    @Query("SELECT l.book, COUNT(l) AS borrowCount FROM Loan l GROUP BY l.book ORDER BY borrowCount DESC")
    List<Object[]> findMostBorrowedBooksWithCount();

    // Used by the admin dashboard to count all currently active loans
    long countByReturnedFalse();

    // ---------- Eager-fetch variants (avoid LazyInitializationException) ----------

    /**
     * Find all loans for a member, with Book and Member loaded eagerly.
     * Useful when rendering loan lists in templates outside a transaction.
     */
    @Query("SELECT l FROM Loan l JOIN FETCH l.book JOIN FETCH l.member WHERE l.member = :member")
    List<Loan> findByMemberWithBookAndMember(@Param("member") Member member);

    /**
     * Find all loans for a member, with the Book loaded eagerly.
     */
    @Query("SELECT l FROM Loan l JOIN FETCH l.book WHERE l.member = :member")
    List<Loan> findByMemberWithBook(@Param("member") Member member);

    /**
     * Find all active (not returned) loans for a member, with the Book loaded eagerly.
     */
    @Query("SELECT l FROM Loan l JOIN FETCH l.book WHERE l.member = :member AND l.returned = false")
    List<Loan> findByMemberAndReturnedFalseWithBook(@Param("member") Member member);
}