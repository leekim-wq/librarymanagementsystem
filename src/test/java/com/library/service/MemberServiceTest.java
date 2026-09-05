package com.library.service;

import com.library.model.Member;
import com.library.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberService memberService;

    private Member member;

    @BeforeEach
    void setUp() {
        member = new Member();
        member.setId(1L);
        member.setUsername("testuser");
        member.setEmail("test@example.com");
        member.setName("Test User");
        member.setPassword("encodedPass");
        member.setRole("MEMBER");
        member.setActive(true);
        member.setBorrowingLimit(5);
        member.setTotalFines(0.0);
        member.setMembershipDate(LocalDate.now());
    }

    @Test
    void getAllMembers_shouldReturnAllMembers() {
        when(memberRepository.findAll()).thenReturn(List.of(member));

        List<Member> result = memberService.getAllMembers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(member.getEmail(), result.get(0).getEmail());
        verify(memberRepository, times(1)).findAll();
    }

    @Test
    void getMemberById_shouldReturnMember_whenExists() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        Optional<Member> result = memberService.getMemberById(1L);

        assertTrue(result.isPresent());
        assertEquals(member.getEmail(), result.get().getEmail());
        verify(memberRepository, times(1)).findById(1L);
    }

    @Test
    void getMemberById_shouldReturnEmpty_whenNotFound() {
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Member> result = memberService.getMemberById(99L);

        assertFalse(result.isPresent());
        verify(memberRepository, times(1)).findById(99L);
    }

    @Test
    void getMemberByEmail_shouldReturnMember_whenExists() {
        when(memberRepository.findByEmail("test@example.com")).thenReturn(Optional.of(member));

        Optional<Member> result = memberService.getMemberByEmail("test@example.com");

        assertTrue(result.isPresent());
        verify(memberRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    void getMemberByUsername_shouldReturnMember_whenExists() {
        when(memberRepository.findByUsername("testuser")).thenReturn(Optional.of(member));

        Optional<Member> result = memberService.getMemberByUsername("testuser");

        assertTrue(result.isPresent());
        verify(memberRepository, times(1)).findByUsername("testuser");
    }

    @Test
    void saveMember_shouldSaveAndReturnMember() {
        when(memberRepository.save(any(Member.class))).thenReturn(member);

        Member saved = memberService.saveMember(member);

        assertNotNull(saved);
        assertEquals(member.getEmail(), saved.getEmail());
        verify(memberRepository, times(1)).save(member);
    }

    @Test
    void deleteMember_shouldCallRepositoryDelete() {
        doNothing().when(memberRepository).deleteById(1L);

        memberService.deleteMember(1L);

        verify(memberRepository, times(1)).deleteById(1L);
    }

    @Test
    void memberExists_shouldReturnTrue_whenEmailExists() {
        when(memberRepository.existsByEmail("test@example.com")).thenReturn(true);

        boolean exists = memberService.memberExists("test@example.com");

        assertTrue(exists);
        verify(memberRepository, times(1)).existsByEmail("test@example.com");
    }

    @Test
    void memberExists_shouldReturnFalse_whenEmailDoesNotExist() {
        when(memberRepository.existsByEmail("notfound@example.com")).thenReturn(false);

        boolean exists = memberService.memberExists("notfound@example.com");

        assertFalse(exists);
        verify(memberRepository, times(1)).existsByEmail("notfound@example.com");
    }

    @Test
    void usernameExists_shouldReturnTrue_whenUsernameExists() {
        when(memberRepository.findByUsername("testuser")).thenReturn(Optional.of(member));

        boolean exists = memberService.usernameExists("testuser");

        assertTrue(exists);
        verify(memberRepository, times(1)).findByUsername("testuser");
    }

    @Test
    void usernameExists_shouldReturnFalse_whenUsernameDoesNotExist() {
        when(memberRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        boolean exists = memberService.usernameExists("unknown");

        assertFalse(exists);
        verify(memberRepository, times(1)).findByUsername("unknown");
    }
}