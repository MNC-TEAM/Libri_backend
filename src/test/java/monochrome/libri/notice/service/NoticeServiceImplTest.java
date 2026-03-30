package monochrome.libri.notice.service;

import monochrome.libri.global.exception.LibriException;
import monochrome.libri.member.domain.Member;
import monochrome.libri.member.domain.MemberStatus;
import monochrome.libri.member.domain.Role;
import monochrome.libri.member.domain.SignType;
import monochrome.libri.member.service.MemberService;
import monochrome.libri.notice.domain.Notice;
import monochrome.libri.notice.dto.request.NoticeCreateRequestDto;
import monochrome.libri.notice.dto.request.NoticeUpdateRequestDto;
import monochrome.libri.notice.repository.NoticeRepository;
import monochrome.libri.notice.service.impl.NoticeServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class NoticeServiceImplTest {

    @Test
    void createNotice_requiresAdmin() {
        NoticeRepository noticeRepository = mock(NoticeRepository.class);
        MemberService memberService = mock(MemberService.class);
        NoticeServiceImpl service = new NoticeServiceImpl(noticeRepository, memberService);

        when(memberService.getMemberById(1L)).thenReturn(Optional.of(member(1L, Role.USER)));

        assertThatThrownBy(() -> service.createNotice(1L, new NoticeCreateRequestDto("title", "content", false, true)))
                .isInstanceOf(LibriException.class);
        verify(noticeRepository, never()).save(any());
    }

    @Test
    void createNotice_savesTrimmedNotice() {
        NoticeRepository noticeRepository = mock(NoticeRepository.class);
        MemberService memberService = mock(MemberService.class);
        NoticeServiceImpl service = new NoticeServiceImpl(noticeRepository, memberService);

        when(memberService.getMemberById(1L)).thenReturn(Optional.of(member(1L, Role.ADMIN)));
        when(noticeRepository.save(any(Notice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.createNotice(1L, new NoticeCreateRequestDto(" title ", " content ", true, false));

        ArgumentCaptor<Notice> captor = ArgumentCaptor.forClass(Notice.class);
        verify(noticeRepository).save(captor.capture());
        assertThat(captor.getValue().getTitle()).isEqualTo("title");
        assertThat(captor.getValue().getContent()).isEqualTo("content");
        assertThat(response.pinned()).isTrue();
        assertThat(response.published()).isFalse();
    }

    @Test
    void updateNotice_updatesFields() {
        NoticeRepository noticeRepository = mock(NoticeRepository.class);
        MemberService memberService = mock(MemberService.class);
        NoticeServiceImpl service = new NoticeServiceImpl(noticeRepository, memberService);

        Notice notice = Notice.builder()
                .id(10L)
                .title("old")
                .content("old-content")
                .pinned(false)
                .published(true)
                .build();

        when(memberService.getMemberById(1L)).thenReturn(Optional.of(member(1L, Role.ADMIN)));
        when(noticeRepository.findById(10L)).thenReturn(Optional.of(notice));

        var response = service.updateNotice(1L, 10L, new NoticeUpdateRequestDto("new", "new-content", true, false));

        assertThat(response.title()).isEqualTo("new");
        assertThat(response.content()).isEqualTo("new-content");
        assertThat(response.pinned()).isTrue();
        assertThat(response.published()).isFalse();
    }

    private Member member(long id, Role role) {
        return Member.builder()
                .id(id)
                .provider(SignType.EMAIL)
                .email("user" + id + "@test.com")
                .emailVerified(true)
                .nickname("nick" + id)
                .username("user" + id)
                .passwordHash("hash")
                .privateAccount(false)
                .memberStatus(MemberStatus.ACTIVE)
                .role(role)
                .build();
    }
}
