package com.library.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "loans")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ⚠️ Excluded from toString / equals / hashCode.
     * Prevents LazyInitializationException when logging or hashing a Loan
     * outside a Hibernate session.
     */
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;

    /**
     * ⚠️ Same reasoning as `book`.
     * Excluded from toString / equals / hashCode.
     */
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "borrow_date")
    private LocalDate borrowDate = LocalDate.now();

    @Column(name = "due_date")
    private LocalDate dueDate = LocalDate.now().plusDays(14);

    @Column(name = "return_date")
    private LocalDate returnDate;

    private double fine = 0.0;
    private boolean returned = false;

    /**
     * Transient field used by Thymeleaf on the "My Loans" page.
     */
    @Transient
    private Long daysLeft;

    // ============================================================
    // Derived methods
    // ============================================================

    public double calculateFine() {
        if (returned || returnDate != null) {
            return fine;
        }
        LocalDate today = LocalDate.now();
        if (today.isAfter(dueDate)) {
            long daysOverdue = ChronoUnit.DAYS.between(dueDate, today);
            return daysOverdue * 0.50; // $0.50 per day
        }
        return 0.0;
    }

    public boolean isOverdue() {
        if (returned || returnDate != null) return false;
        return LocalDate.now().isAfter(dueDate);
    }

    public Long getDaysLeft() {
        return daysLeft;
    }

    public void setDaysLeft(Long daysLeft) {
        this.daysLeft = daysLeft;
    }

    // ============================================================
    // Custom toString — never touches lazy fields
    // ============================================================
    @Override
    public String toString() {
        return "Loan{" +
                "id=" + id +
                ", borrowDate=" + borrowDate +
                ", dueDate=" + dueDate +
                ", returnDate=" + returnDate +
                ", returned=" + returned +
                ", fine=" + fine +
                '}';
    }
}