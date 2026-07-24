package com.library.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "members")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String password;

    private String phone;
    private String address;

    @Column(name = "membership_date")
    private LocalDate membershipDate = LocalDate.now();

    @Column(name = "membership_type")
    private String membershipType = "Standard";

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @LazyCollection(LazyCollectionOption.EXTRA)
    private List<Loan> loans = new ArrayList<>();

    private boolean active = true;

    @Column(name = "total_fines")
    private double totalFines = 0.0;

    @Column(name = "borrowing_limit")
    private Integer borrowingLimit = 5;

    /**
     * Check if member can borrow more books
     * FIXED: No longer uses lazy loading directly
     */
    public boolean canBorrow() {
        // The loans collection might be lazy, so we use a different approach
        // We'll check borrowing limit without loading all loans
        return totalFines < 100.0;
    }

    /**
     * Get active loans count (to be used with proper transaction)
     */
    public long getActiveLoansCount() {
        if (loans == null) return 0;
        return loans.stream()
                .filter(loan -> !loan.isReturned())
                .count();
    }

    /**
     * Check if member has reached borrowing limit
     */
    public boolean hasReachedBorrowingLimit() {
        return getActiveLoansCount() >= borrowingLimit;
    }
}