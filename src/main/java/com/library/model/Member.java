package com.library.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "members")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Member implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true)
    private String username;

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

    /**
     * ⚠️ Lazy fetch + excluded from toString / equals / hashCode.
     * Why:
     *  - FetchType.LAZY avoids loading all loans every time a Member is fetched.
     *  - @ToString.Exclude prevents LazyInitializationException when Spring
     *    Security logs the authenticated user outside a transaction.
     *  - @EqualsAndHashCode.Exclude prevents the same problem in equals()/hashCode().
     *  - @JsonIgnore isn't needed (no REST serialization of this entity yet).
     */
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Loan> loans = new ArrayList<>();

    private boolean active = true;

    @Column(name = "total_fines")
    private double totalFines = 0.0;

    @Column(name = "borrowing_limit")
    private Integer borrowingLimit = 5;

    @Column(name = "role")
    private String role = "MEMBER";

    // ============================================================
    // UserDetails implementation
    // ============================================================

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getUsername() {
        return username != null && !username.isEmpty() ? username : email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }

    // ============================================================
    // Helper: how many active loans this member currently has
    // (Only safe to call inside a transaction.)
    // ============================================================
    public long getActiveLoanCount() {
        if (loans == null) return 0;
        return loans.stream().filter(loan -> !loan.isReturned()).count();
    }

    public boolean canBorrow() {
        return getActiveLoanCount() < borrowingLimit && totalFines < 100.0;
    }

    // ============================================================
    // Custom toString — never touches lazy fields
    // ============================================================
    @Override
    public String toString() {
        return "Member{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", name='" + name + '\'' +
                ", role='" + role + '\'' +
                ", active=" + active +
                '}';
    }
}