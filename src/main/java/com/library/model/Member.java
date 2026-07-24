package com.library.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
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

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<Loan> loans = new ArrayList<>();

    private boolean active = true;
    private double totalFines = 0.0;

    @Column(name = "borrowing_limit")
    private Integer borrowingLimit = 5;

    public boolean canBorrow() {
        long activeLoans = loans.stream()
                .filter(loan -> loan.getReturnDate() == null)
                .count();
        return activeLoans < borrowingLimit && totalFines < 100.0;
    }
}