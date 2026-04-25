package monochrome.libri.member.controller;

import monochrome.libri.TestFixtures;
import monochrome.libri.block.dto.response.BlockedMemberListResponseDto;
import monochrome.libri.block.service.BlockService;
import monochrome.libri.global.exception.LibriException;
import monochrome.libri.global.security.UserPrincipal;
import monochrome.libri.member.domain.Member;
import monochrome.libri.member.dto.request.MemberUpdateRequestDto;
import monochrome.libri.member.dto.request.NicknameUpdateRequestDto;
import monochrome.libri.member.dto.request.PrivacyUpdateRequestDto;
import monochrome.libri.member.dto.request.ProfileImageUpdateRequestDto;
import monochrome.libri.member.dto.response.MemberPrivacyResponseDto;
import monochrome.libri.member.dto.response.MemberProfileResponseDto;
import monochrome.libri.member.dto.response.MemberResponseDto;
import monochrome.libri.member.service.MemberService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberControllerTest {

    @Mock
    private MemberService memberService;

    @Mock
    private BlockService blockService;

    @InjectMocks
    private MemberController controller;

    @Test
    void getMe_requiresAuth() {
        assertThatThrownBy(() -> controller.getMe(null))
                .isInstanceOf(LibriException.class);
        verifyNoInteractions(memberService);
    }

    @Test
    void getMe_returnsAuthenticatedMember() {
        UserPrincipal principal = new UserPrincipal(1L, null, List.of(), true);
        MemberResponseDto member = new MemberResponseDto(1L, null, "user1@test.com", "nick1", "/profile/1", false, 2L, 3L);

        when(memberService.getMyProfile(1L)).thenReturn(member);

        var response = controller.getMe(principal);

        verify(memberService).getMyProfile(1L);
        assertThat(response.getBody().data().id()).isEqualTo(1L);
        assertThat(response.getBody().data().email()).isEqualTo(member.email());
        assertThat(response.getBody().data().followerCount()).isEqualTo(2L);
    }

    @Test
    void getMyPrivacy_returnsPrivateAccountFlag() {
        UserPrincipal principal = new UserPrincipal(1L, null, List.of(), true);
        when(memberService.getMyPrivacy(1L)).thenReturn(new MemberPrivacyResponseDto(1L, true));

        var response = controller.getMyPrivacy(principal);

        verify(memberService).getMyPrivacy(1L);
        assertThat(response.getBody().data().privateAccount()).isTrue();
    }

    @Test
    void getMemberProfile_returnsRequestedMemberProfile() {
        UserPrincipal principal = new UserPrincipal(1L, null, List.of(), true);
        MemberProfileResponseDto profile = new MemberProfileResponseDto(2L, "user2", "nick2", "/profile/2", true, 4L, 5L, false, true);
        when(memberService.getMemberProfile(1L, 2L)).thenReturn(profile);

        var response = controller.getMemberProfile(2L, principal);

        verify(memberService).getMemberProfile(1L, 2L);
        assertThat(response.getBody().data().following()).isTrue();
        assertThat(response.getBody().data().privateAccount()).isTrue();
    }

    @Test
    void blockMember_requiresAuth() {
        assertThatThrownBy(() -> controller.blockMember(2L, null))
                .isInstanceOf(LibriException.class);
        verifyNoInteractions(blockService);
    }

    @Test
    void getMyBlockedMembers_returnsBlockedList() {
        UserPrincipal principal = new UserPrincipal(1L, null, List.of(), true);
        when(blockService.getBlockedMembers(eq(1L), any())).thenReturn(
                new BlockedMemberListResponseDto(1L, List.of(), false, 0, 20)
        );

        var response = controller.getMyBlockedMembers(principal, 0, 20);

        assertThat(response.getBody().data().totalCount()).isEqualTo(1L);
        verify(blockService).getBlockedMembers(eq(1L), any());
    }

    @Test
    void updateNickname_requiresAuth() {
        NicknameUpdateRequestDto request = new NicknameUpdateRequestDto("newNick");

        assertThatThrownBy(() -> controller.updateNickname(null, request))
                .isInstanceOf(LibriException.class);
        verifyNoInteractions(memberService);
    }

    @Test
    void updateNickname_updatesNicknameForAuthenticatedUser() {
        UserPrincipal principal = new UserPrincipal(1L, null, List.of(), true);
        Member updated = TestFixtures.member(1L);
        updated.updateMember(null, "newNick", null, null, null);
        MemberResponseDto responseDto = new MemberResponseDto(1L, updated.getProvider(), updated.getEmail(), "newNick", updated.getProfilePath(), false, 2L, 3L);

        when(memberService.updateMember(eq(1L), any(MemberUpdateRequestDto.class))).thenReturn(updated);
        when(memberService.getMyProfile(1L)).thenReturn(responseDto);

        var response = controller.updateNickname(principal, new NicknameUpdateRequestDto("newNick"));

        ArgumentCaptor<MemberUpdateRequestDto> captor = ArgumentCaptor.forClass(MemberUpdateRequestDto.class);
        verify(memberService).updateMember(eq(1L), captor.capture());
        assertThat(captor.getValue().nickname()).isEqualTo("newNick");
        assertThat(response.getBody().data().nickname()).isEqualTo("newNick");
        assertThat(response.getBody().data().followingCount()).isEqualTo(3L);
    }

    @Test
    void updatePrivacy_updatesPrivateAccountForAuthenticatedUser() {
        UserPrincipal principal = new UserPrincipal(1L, null, List.of(), true);
        Member updated = TestFixtures.member(1L);
        updated.updateMember(null, null, null, null, true);
        MemberResponseDto responseDto = new MemberResponseDto(1L, updated.getProvider(), updated.getEmail(), updated.getNickname(), updated.getProfilePath(), true, 2L, 3L);

        when(memberService.updateMember(eq(1L), any(MemberUpdateRequestDto.class))).thenReturn(updated);
        when(memberService.getMyProfile(1L)).thenReturn(responseDto);

        var response = controller.updatePrivacy(principal, new PrivacyUpdateRequestDto(true));

        ArgumentCaptor<MemberUpdateRequestDto> captor = ArgumentCaptor.forClass(MemberUpdateRequestDto.class);
        verify(memberService).updateMember(eq(1L), captor.capture());
        assertThat(captor.getValue().privateAccount()).isTrue();
        assertThat(response.getBody().data().privateAccount()).isTrue();
    }

    @Test
    void updateProfileImage_updatesProfilePathForAuthenticatedUser() {
        UserPrincipal principal = new UserPrincipal(1L, null, List.of(), true);
        Member updated = TestFixtures.member(1L);
        updated.updateMember(null, null, null, "https://bucket/path.png", null);
        MemberResponseDto responseDto = new MemberResponseDto(
                1L,
                updated.getProvider(),
                updated.getEmail(),
                updated.getNickname(),
                "https://bucket/path.png",
                false,
                2L,
                3L
        );

        when(memberService.updateMember(eq(1L), any(MemberUpdateRequestDto.class))).thenReturn(updated);
        when(memberService.getMyProfile(1L)).thenReturn(responseDto);

        var response = controller.updateProfileImage(principal, new ProfileImageUpdateRequestDto("https://bucket/path.png"));

        ArgumentCaptor<MemberUpdateRequestDto> captor = ArgumentCaptor.forClass(MemberUpdateRequestDto.class);
        verify(memberService).updateMember(eq(1L), captor.capture());
        assertThat(captor.getValue().profilePath()).isEqualTo("https://bucket/path.png");
        assertThat(response.getBody().data().profilePath()).isEqualTo("https://bucket/path.png");
    }
}
