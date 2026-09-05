package com.library.model;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "members")
public class Member implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;   // new field

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
    private List<Loan> loans = new ArrayList<>();

    private boolean active = true;

    @Column(name = "total_fines")
    private double totalFines = 0.0;

    @Column(name = "borrowing_limit")
    private Integer borrowingLimit = 5;

    @Column(name = "role")
    private String role = "MEMBER";

    // ---------- Getters and Setters ----------
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    // We do NOT have a separate getUsername() – the @Override below serves as the getter

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public LocalDate getMembershipDate() { return membershipDate; }
    public void setMembershipDate(LocalDate membershipDate) { this.membershipDate = membershipDate; }

    public String getMembershipType() { return membershipType; }
    public void setMembershipType(String membershipType) { this.membershipType = membershipType; }

    public List<Loan> getLoans() { return loans; }
    public void setLoans(List<Loan> loans) { this.loans = loans; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public double getTotalFines() { return totalFines; }
    public void setTotalFines(double totalFines) { this.totalFines = totalFines; }

    public Integer getBorrowingLimit() { return borrowingLimit; }
    public void setBorrowingLimit(Integer borrowingLimit) { this.borrowingLimit = borrowingLimit; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getUsername() { return username; }   // <-- Only ONE getter
    public void setUsername(String username) { this.username = username; }

    // ---------- Business logic ----------
    public boolean canBorrow() {
        long activeLoans = loans.stream()
                .filter(loan -> !loan.isReturned())
                .count();
        return activeLoans < borrowingLimit && totalFines < 100.0;
    }

    // ---------- UserDetails implementation ----------
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    // Already overridden above – but we need to keep it as the UserDetails method.
    // The method getUsername() is already defined above (line ~70).
    // So we do NOT redeclare it here.

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return active; }
}