package monochrome.libri.member.controller;

import monochrome.libri.TestFixtures;
import monochrome.libri.global.exception.LibriException;
import monochrome.libri.global.security.UserPrincipal;
import monochrome.libri.member.domain.Member;
import monochrome.libri.member.dto.request.MemberUpdateRequestDto;
import monochrome.libri.member.dto.request.NicknameUpdateRequestDto;
import monochrome.libri.member.dto.request.PrivacyUpdateRequestDto;
import monochrome.libri.member.service.MemberService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberControllerTest {

    @Mock
    private MemberService memberService;

    @InjectMocks
    private MemberController controller;

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

        when(memberService.updateMember(eq(1L), any(MemberUpdateRequestDto.class))).thenReturn(updated);

        var response = controller.updateNickname(principal, new NicknameUpdateRequestDto("newNick"));

        ArgumentCaptor<MemberUpdateRequestDto> captor = ArgumentCaptor.forClass(MemberUpdateRequestDto.class);
        verify(memberService).updateMember(eq(1L), captor.capture());
        assertThat(captor.getValue().nickname()).isEqualTo("newNick");
        assertThat(response.getBody().data().nickname()).isEqualTo("newNick");
    }

    @Test
    void updatePrivacy_updatesPrivateAccountForAuthenticatedUser() {
        UserPrincipal principal = new UserPrincipal(1L, null, List.of(), true);
        Member updated = TestFixtures.member(1L);
        updated.updateMember(null, null, null, null, true);

        when(memberService.updateMember(eq(1L), any(MemberUpdateRequestDto.class))).thenReturn(updated);

        var response = controller.updatePrivacy(principal, new PrivacyUpdateRequestDto(true));

        ArgumentCaptor<MemberUpdateRequestDto> captor = ArgumentCaptor.forClass(MemberUpdateRequestDto.class);
        verify(memberService).updateMember(eq(1L), captor.capture());
        assertThat(captor.getValue().privateAccount()).isTrue();
        assertThat(response.getBody().data().privateAccount()).isTrue();
    }
}
