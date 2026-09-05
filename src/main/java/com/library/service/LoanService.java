package com.library.service;

import com.library.model.Loan;
import com.library.model.Member;
import com.library.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class LoanService {

    @Autowired
    private LoanRepository loanRepository;

    // --- Basic CRUD ---

    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }

    public Optional<Loan> getLoanById(Long id) {
        return loanRepository.findById(id);
    }

    public Loan saveLoan(Loan loan) {
        return loanRepository.save(loan);
    }

    public void deleteLoan(Long id) {
        loanRepository.deleteById(id);
    }

    // --- Member-specific queries (NOW EAGER) ---

    /**
     * Get all loans (including returned) for a member, with Book eagerly loaded.
     */
    public List<Loan> getAllLoansForMember(Long memberId) {
        Member member = new Member();
        member.setId(memberId);
        return loanRepository.findByMemberWithBook(member);
    }

    /**
     * Get only active (not returned) loans for a member, with Book eagerly loaded.
     */
    public List<Loan> getActiveLoansForMember(Long memberId) {
        Member member = new Member();
        member.setId(memberId);
        return loanRepository.findByMemberAndReturnedFalseWithBook(member);
    }

    /**
     * Check if a loan belongs to a given member (security check).
     */
    public boolean isLoanBelongsToMember(Long loanId, Long memberId) {
        return loanRepository.findById(loanId)
                .map(loan -> loan.getMember().getId().equals(memberId))
                .orElse(false);
    }

    // --- Overdue and counting ---

    public List<Loan> getOverdueLoans() {
        return loanRepository.findOverdueLoans();
    }

    public long countActiveLoansByMember(Long memberId) {
        Member member = new Member();
        member.setId(memberId);
        return loanRepository.findByMemberAndReturnedFalse(member).size();
    }

    // --- Deprecated methods (kept for compatibility) ---
    @Deprecated
    public List<Loan> getMemberLoans(Long memberId) {
        return getActiveLoansForMember(memberId);
    }

    @Deprecated
    public List<Loan> getMemberAllLoans(Long memberId) {
        return getAllLoansForMember(memberId);
    }
}