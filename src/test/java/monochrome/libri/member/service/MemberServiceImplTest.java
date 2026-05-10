package monochrome.libri.member.service;

import monochrome.libri.TestFixtures;
import monochrome.libri.block.service.BlockService;
import monochrome.libri.follow.repository.FollowRepository;
import monochrome.libri.global.exception.LibriException;
import monochrome.libri.global.security.PasswordHashService;
import monochrome.libri.member.domain.Member;
import monochrome.libri.member.domain.MemberReport;
import monochrome.libri.member.domain.MemberReportReason;
import monochrome.libri.member.domain.MemberStatus;
import monochrome.libri.member.dto.request.MemberReportCreateRequestDto;
import monochrome.libri.member.dto.request.MemberUpdateRequestDto;
import monochrome.libri.member.repository.MemberReportRepository;
import monochrome.libri.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
    private FollowRepository followRepository;

    @Mock
    private MemberReportRepository memberReportRepository;

    @Mock
    private BlockService blockService;

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
                true,
                null,
                null
        );

        Member updated = service.updateMember(1L, dto);

        assertThat(updated.getPasswordHash()).isEqualTo("hashed-new");
        assertThat(updated.getUsername()).isEqualTo("newuser");
        assertThat(updated.getNickname()).isEqualTo("newnick");
        assertThat(updated.getProfilePath()).isEqualTo("/new/profile");
        assertThat(updated.isPrivateAccount()).isTrue();
    }

    @Test
    void updateMember_throwsWhenNotFound() {
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        MemberUpdateRequestDto dto = new MemberUpdateRequestDto(
                null, null, null, null,
                null, null, null, null, null, null, null
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

    @Test
    void reportMember_savesReport() {
        Member reporter = TestFixtures.member(1L);
        Member reported = TestFixtures.member(2L);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(reporter));
        when(memberRepository.findById(2L)).thenReturn(Optional.of(reported));
        when(memberReportRepository.existsByReportedMemberAndReporter(reported, reporter)).thenReturn(false);

        service.reportMember(1L, 2L, new MemberReportCreateRequestDto(MemberReportReason.ABUSE, "  abusive profile  "));

        ArgumentCaptor<MemberReport> captor = ArgumentCaptor.forClass(MemberReport.class);
        verify(memberReportRepository).save(captor.capture());
        assertThat(captor.getValue().getReason()).isEqualTo(MemberReportReason.ABUSE);
        assertThat(captor.getValue().getDetail()).isEqualTo("abusive profile");
    }

    @Test
    void reportMember_rejectsDuplicateReport() {
        Member reporter = TestFixtures.member(1L);
        Member reported = TestFixtures.member(2L);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(reporter));
        when(memberRepository.findById(2L)).thenReturn(Optional.of(reported));
        when(memberReportRepository.existsByReportedMemberAndReporter(reported, reporter)).thenReturn(true);

        assertThatThrownBy(() -> service.reportMember(1L, 2L, new MemberReportCreateRequestDto(MemberReportReason.SPAM, null)))
                .isInstanceOf(LibriException.class);
        verify(memberReportRepository, never()).save(any());
    }
}
