package com.library.repository;

import com.library.model.Loan;
import com.library.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByMemberAndReturnedFalse(Member member);
    List<Loan> findByMember(Member member);
    Optional<Loan> findByBookIdAndMemberIdAndReturnedFalse(Long bookId, Long memberId);

    @Query("SELECT l FROM Loan l WHERE l.returned = false AND l.dueDate < CURRENT_DATE")
    List<Loan> findOverdueLoans();
}