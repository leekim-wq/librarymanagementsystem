package com.library.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
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

    @ManyToOne
    @JoinColumn(name = "book_id")
    private Book book;

    @ManyToOne
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
        if (returnDate != null) return false;
        return LocalDate.now().isAfter(dueDate);
    }
}
