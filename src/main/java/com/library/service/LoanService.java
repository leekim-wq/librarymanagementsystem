package com.library.service;

import com.library.model.Loan;
import com.library.model.Member;
import com.library.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LoanService {

    @Autowired
    private LoanRepository loanRepository;

    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }

    public List<Loan> getMemberLoans(Long memberId) {
        Member member = new Member();
        member.setId(memberId);
        return loanRepository.findByMemberAndReturnedFalse(member);
    }

    public List<Loan> getMemberAllLoans(Long memberId) {
        Member member = new Member();
        member.setId(memberId);
        return loanRepository.findByMember(member);
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

    public List<Loan> getOverdueLoans() {
        return loanRepository.findOverdueLoans();
    }

    public long countActiveLoansByMember(Long memberId) {
        Member member = new Member();
        member.setId(memberId);
        return loanRepository.findByMemberAndReturnedFalse(member).size();
    }
}