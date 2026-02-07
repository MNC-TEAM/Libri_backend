package monochrome.libri.member.service;

import monochrome.libri.TestFixtures;
import monochrome.libri.global.exception.LibriException;
import monochrome.libri.global.security.PasswordHashService;
import monochrome.libri.member.domain.Member;
import monochrome.libri.member.domain.MemberStatus;
import monochrome.libri.member.dto.request.MemberUpdateRequestDto;
import monochrome.libri.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceImplTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordHashService passwordHashService;

    @InjectMocks
    private monochrome.libri.member.service.impl.MemberServiceImpl service;

    @Test
    void updateMember_updatesPasswordWhenProvided() {
        Member member = TestFixtures.member(1L);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(passwordHashService.hashPassword("newpass")).thenReturn("hashed-new");

        MemberUpdateRequestDto dto = new MemberUpdateRequestDto(
                null, null, null, null,
                "newuser",
                "newnick",
                "newpass",
                "/new/profile",
                null,
                null
        );

        Member updated = service.updateMember(1L, dto);

        assertThat(updated.getPasswordHash()).isEqualTo("hashed-new");
        assertThat(updated.getUsername()).isEqualTo("newuser");
        assertThat(updated.getNickname()).isEqualTo("newnick");
        assertThat(updated.getProfilePath()).isEqualTo("/new/profile");
    }

    @Test
    void updateMember_throwsWhenNotFound() {
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        MemberUpdateRequestDto dto = new MemberUpdateRequestDto(
                null, null, null, null,
                null, null, null, null, null, null
        );

        assertThatThrownBy(() -> service.updateMember(1L, dto))
                .isInstanceOf(LibriException.class);
    }

    @Test
    void withdraw_setsStatusDeleted() {
        Member member = TestFixtures.member(1L);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        service.withdraw(1L);

        assertThat(member.getMemberStatus()).isEqualTo(MemberStatus.DELETE);
    }
}
