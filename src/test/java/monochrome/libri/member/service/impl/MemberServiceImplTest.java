package monochrome.libri.member.service.impl;

import monochrome.libri.global.exception.LibriException;
import monochrome.libri.global.security.PasswordHashService;
import monochrome.libri.member.domain.Member;
import monochrome.libri.member.domain.MemberStatus;
import monochrome.libri.member.domain.Role;
import monochrome.libri.member.dto.request.MemberUpdateRequestDto;
import monochrome.libri.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceImplTest {

    @Mock
    MemberRepository memberRepository;
    @Mock
    PasswordHashService passwordHashService;
    @InjectMocks
    MemberServiceImpl memberService;

    @Test
    void 비밀번호가_없으면_기존_hash_유지() {

        //given
        Member member = Member.builder()
                .id(1L)
                .primaryEmail("old@old.com")
                .emailVerified(true)
                .emailFromProvider("social@old.com")
                .emailVerifiedFromProvider(true)
                .username("oldUser")
                .nickname("oldNick")
                .passwordHash("EXISTING_HASH")
                .profilePath("old.png")
                .memberStatus(MemberStatus.ACTIVE)
                .role(Role.USER)
                .build();

        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        MemberUpdateRequestDto dto = new MemberUpdateRequestDto(
                "new@new.com",   // primaryEmail 변경
                null,            // emailVerified는 변경 없음
                null,            // emailFromProvider 변경 없음
                null,            // emailVerifiedFromProvider 변경 없음
                "newUser",       // username 변경
                null,            // nickname 변경 없음
                null,            // rawPassword 없음 → 비밀번호 변경 X
                "new.png",       // profilePath 변경
                null,            // status 변경 없음
                null             // role 변경 없음
        );

        //when
        Member updated = memberService.updateMember(1L, dto);


        //then
        // then
//        assertThat(updated.getPrimaryEmail()).isEqualTo("new@new.com");
        assertThat(updated.getUsername()).isEqualTo("newUser");
        assertThat(updated.getProfilePath()).isEqualTo("new.png");

        // null로 들어온 필드는 기존 값 유지
        assertThat(updated.isEmailVerified()).isTrue();
        assertThat(updated.getEmailFromProvider()).isEqualTo("social@old.com");
        assertThat(updated.getEmailVerifiedFromProvider()).isTrue();
        assertThat(updated.getNickname()).isEqualTo("oldNick");
        assertThat(updated.getMemberStatus()).isEqualTo(MemberStatus.ACTIVE);
        assertThat(updated.getRole()).isEqualTo(Role.USER);

        // 비밀번호 해시는 그대로 유지
        assertThat(updated.getPasswordHash()).isEqualTo("EXISTING_HASH");

        // 해시 서비스는 호출되지 않아야 함
        then(passwordHashService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("updateMember - 비밀번호가 들어오면 해시 후 updateMember 호출")
    void updateMember_withRawPassword_updatesPasswordHash() {
        // given
        long memberId = 1L;
        Member member = mock(Member.class);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(member.getPasswordHash()).thenReturn("old_hash");

        MemberUpdateRequestDto dto = new MemberUpdateRequestDto(
                null,           // primaryEmail
                null,           // emailVerified
                null,           // emailFromProvider
                null,           // emailVerifiedFromProvider
                "newUsername",  // username
                "newNickname",  // nickname
                "newRawPassword", // rawPassword
                "newProfilePath", // profilePath
                null,           // status
                null            // role
        );

        when(passwordHashService.hashPassword("newRawPassword")).thenReturn("new_hash");

        Member updatedMember = mock(Member.class);
        when(member.updateMember(
                "newUsername",
                "newNickname",
                "new_hash",
                "newProfilePath"
        )).thenReturn(updatedMember);

        // when
        Member result = memberService.updateMember(memberId, dto);

        // then
        assertSame(updatedMember, result);
        verify(memberRepository).findById(memberId);
        verify(passwordHashService).hashPassword("newRawPassword");
        verify(member).updateMember(
                "newUsername",
                "newNickname",
                "new_hash",
                "newProfilePath"
        );
    }

    @Test
    @DisplayName("updateMember - 비밀번호가 null 이거나 공백이면 기존 비밀번호 유지")
    void updateMember_withoutRawPassword_keepsOldPasswordHash() {
        // given
        long memberId = 1L;
        Member member = mock(Member.class);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(member.getPasswordHash()).thenReturn("old_hash");

        MemberUpdateRequestDto dto = new MemberUpdateRequestDto(
                null,           // primaryEmail
                null,           // emailVerified
                null,           // emailFromProvider
                null,           // emailVerifiedFromProvider
                "newUsername",  // username
                "newNickname",  // nickname
                null,           // rawPassword (없음)
                "newProfilePath",
                null,
                null
        );

        Member updatedMember = mock(Member.class);
        when(member.updateMember(
                "newUsername",
                "newNickname",
                "old_hash",
                "newProfilePath"
        )).thenReturn(updatedMember);

        // when
        Member result = memberService.updateMember(memberId, dto);

        // then
        assertSame(updatedMember, result);
        verify(memberRepository).findById(memberId);
        verify(member).getPasswordHash();
        verify(passwordHashService, never()).hashPassword(anyString());
        verify(member).updateMember(
                "newUsername",
                "newNickname",
                "old_hash",
                "newProfilePath"
        );
    }

    @Test
    @DisplayName("updateMember - 존재하지 않는 회원이면 LibriException 발생")
    void updateMember_memberNotFound_throwsException() {
        // given
        long memberId = 1L;
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        MemberUpdateRequestDto dto = new MemberUpdateRequestDto(
                "test@example.com", // primaryEmail
                true,               // emailVerified
                null,
                null,
                "username",
                "nickname",
                "rawPw",
                "profilePath",
                null,
                null
        );

        // when & then
        assertThrows(LibriException.class,
                () -> memberService.updateMember(memberId, dto));

        verify(memberRepository).findById(memberId);
        verifyNoMoreInteractions(memberRepository);
        verifyNoInteractions(passwordHashService);
    }

    @Test
    @DisplayName("getMemberById - Repository 결과를 그대로 반환")
    void getMemberById_returnsRepositoryResult() {
        // given
        long memberId = 1L;
        Member member = mock(Member.class);
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        // when
        Optional<Member> result = memberService.getMemberById(memberId);

        // then
        assertTrue(result.isPresent());
        assertSame(member, result.get());
        verify(memberRepository).findById(memberId);
    }

    @Test
    @DisplayName("getMemberByEmail - Repository 결과를 그대로 반환")
    void getMemberByEmail_returnsRepositoryResult() {
        // given
        String email = "test@example.com";
        Member member = mock(Member.class);
        when(memberRepository.findByPrimaryEmail(email)).thenReturn(Optional.of(member));

        // when
        Optional<Member> result = memberService.getMemberByEmail(email);

        // then
        assertTrue(result.isPresent());
        assertSame(member, result.get());
        verify(memberRepository).findByPrimaryEmail(email);
    }

    @Test
    @DisplayName("withdraw - 회원이 존재하면 withdraw() 호출")
    void withdraw_existingMember_callsWithdraw() {
        // given
        Long memberId = 1L;
        Member member = mock(Member.class);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        // when
        memberService.withdraw(memberId);

        // then
        verify(memberRepository).findById(memberId);
        verify(member).withdraw();
    }

    @Test
    @DisplayName("withdraw - 회원이 없으면 LibriException 발생")
    void withdraw_memberNotFound_throwsException() {
        // given
        Long memberId = 1L;
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(LibriException.class,
                () -> memberService.withdraw(memberId));

        verify(memberRepository).findById(memberId);
    }
}