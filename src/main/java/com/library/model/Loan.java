package com.library.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "loans")
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
    private LocalDate borrowDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "return_date")
    private LocalDate returnDate;

    private boolean returned = false;

    private double fine = 0.0;

    // ---------- Getters and Setters ----------
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }

    public Member getMember() { return member; }
    public void setMember(Member member) { this.member = member; }

    public LocalDate getBorrowDate() { return borrowDate; }
    public void setBorrowDate(LocalDate borrowDate) { this.borrowDate = borrowDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }

    public boolean isReturned() { return returned; }
    public void setReturned(boolean returned) { this.returned = returned; }

    public double getFine() { return fine; }
    public void setFine(double fine) { this.fine = fine; }

    public boolean isOverdue() {
        return !returned && dueDate != null && LocalDate.now().isAfter(dueDate);
    }

    public double calculateFine() {
        if (returned || dueDate == null) return 0.0;
        if (LocalDate.now().isAfter(dueDate)) {
            long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(dueDate, LocalDate.now());
            return daysOverdue * 0.50; // 50 cents per day
        }
        return 0.0;
    }
}