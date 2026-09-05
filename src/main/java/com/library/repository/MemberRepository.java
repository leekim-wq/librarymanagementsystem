package com.library.repository;

import com.library.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    // Find by email (used for authentication)
    Optional<Member> findByEmail(String email);

    // NEW: Find by username
    Optional<Member> findByUsername(String username);

    // Check if email exists (used during registration)
    boolean existsByEmail(String email);

    // Optional: check if username exists
    boolean existsByUsername(String username);
}