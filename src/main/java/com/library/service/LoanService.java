package com.library.service;

import com.library.model.Book;
import com.library.model.Loan;
import com.library.model.Member;
import com.library.repository.BookRepository;
import com.library.repository.LoanRepository;
import com.library.repository.MemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class LoanService {

    private static final Logger log = LoggerFactory.getLogger(LoanService.class);

    @Autowired private LoanRepository loanRepository;
    @Autowired private BookRepository bookRepository;
    @Autowired private MemberRepository memberRepository;

    // ---------- Basic CRUD ----------

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

    // ---------- Member-scoped ----------

    public List<Loan> getAllLoansForMember(Long memberId) {
        Member member = new Member();
        member.setId(memberId);
        return loanRepository.findByMemberWithBook(member);
    }

    public List<Loan> getActiveLoansForMember(Long memberId) {
        Member member = new Member();
        member.setId(memberId);
        return loanRepository.findByMemberAndReturnedFalseWithBook(member);
    }

    public boolean isLoanBelongsToMember(Long loanId, Long memberId) {
        return loanRepository.findById(loanId)
                .map(loan -> loan.getMember().getId().equals(memberId))
                .orElse(false);
    }

    // ---------- Staff-wide (Admin / Librarian) ----------

    public List<Loan> getAllActiveLoans() {
        return loanRepository.findAllActiveLoans();
    }

    public List<Loan> getAllLoansWithDetails() {
        return loanRepository.findAllWithDetails();
    }

    // ---------- Overdue & counting ----------

    public List<Loan> getOverdueLoans() {
        return loanRepository.findOverdueLoans();
    }

    public long countActiveLoansByMember(Long memberId) {
        Member member = new Member();
        member.setId(memberId);
        return loanRepository.findByMemberAndReturnedFalse(member).size();
    }

    // ---------- Return (staff only — enforced by controller) ----------

    @Transactional
    public boolean returnBook(Long loanId) {
        if (loanId == null) return false;

        Optional<Loan> loanOpt = loanRepository.findById(loanId);
        if (loanOpt.isEmpty()) {
            log.warn("Loan not found: {}", loanId);
            return false;
        }

        Loan loan = loanOpt.get();
        if (loan.isReturned()) {
            log.warn("Loan already returned: {}", loanId);
            return false;
        }

        loan.setReturned(true);
        loan.setReturnDate(LocalDate.now());
        double fine = loan.calculateFine();
        loan.setFine(fine);
        loanRepository.save(loan);

        Book book = loan.getBook();
        book.setAvailableQuantity(book.getAvailableQuantity() + 1);
        bookRepository.save(book);

        Member member = loan.getMember();
        member.setTotalFines(member.getTotalFines() + fine);
        memberRepository.save(member);

        log.debug("Book returned: loanId={}, fine={}", loanId, fine);
        return true;
    }

    // ============================================================
    // DEPRECATED ALIASES — kept so existing tests don't break
    // ============================================================

    @Deprecated
    public List<Loan> getMemberLoans(Long memberId) {
        return getActiveLoansForMember(memberId);
    }

    @Deprecated
    public List<Loan> getMemberAllLoans(Long memberId) {
        return getAllLoansForMember(memberId);
    }
}