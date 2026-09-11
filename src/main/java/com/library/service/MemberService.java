package com.library.service;

import com.library.model.Member;
import com.library.repository.LoanRepository;
import com.library.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class MemberService {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private LoanRepository loanRepository;

    // ---------- Read operations ----------

    @Transactional(readOnly = true)
    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Member> getMemberById(Long id) {
        return memberRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Member> getMemberByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public Optional<Member> getMemberByUsername(String username) {
        return memberRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public boolean memberExists(String email) {
        return memberRepository.existsByEmail(email);
    }

    /**
     * Uses the more efficient existsByUsername query.
     * Matches MemberServiceTest expectations.
     */
    @Transactional(readOnly = true)
    public boolean usernameExists(String username) {
        return memberRepository.existsByUsername(username);
    }

    // ---------- Write operations ----------

    @Transactional
    public Member saveMember(Member member) {
        return memberRepository.save(member);
    }

    @Transactional
    public void deleteMember(Long id) {
        memberRepository.deleteById(id);
    }

    // ---------- Loan helpers (transaction-safe) ----------

    @Transactional(readOnly = true)
    public long countActiveLoans(Member member) {
        if (member == null || member.getId() == null) return 0;
        return loanRepository.countByMemberAndReturnedFalse(member);
    }

    @Transactional(readOnly = true)
    public long countOverdueLoans(Member member) {
        if (member == null) return 0;
        return loanRepository.findOverdueLoansByMember(member).size();
    }

    @Transactional(readOnly = true)
    public double getTotalFines(Member member) {
        if (member == null) return 0.0;
        Double total = loanRepository.getTotalFinesByMember(member);
        return total == null ? 0.0 : total;
    }
}