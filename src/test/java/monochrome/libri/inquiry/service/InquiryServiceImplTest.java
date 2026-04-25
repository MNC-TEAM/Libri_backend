package monochrome.libri.inquiry.service;

import monochrome.libri.global.exception.LibriException;
import monochrome.libri.inquiry.domain.Inquiry;
import monochrome.libri.inquiry.domain.InquiryStatus;
import monochrome.libri.inquiry.dto.request.InquiryAnswerRequestDto;
import monochrome.libri.inquiry.dto.request.InquiryCreateRequestDto;
import monochrome.libri.inquiry.dto.request.InquiryStatusUpdateRequestDto;
import monochrome.libri.inquiry.repository.InquiryRepository;
import monochrome.libri.inquiry.service.impl.InquiryServiceImpl;
import monochrome.libri.member.domain.Member;
import monochrome.libri.member.domain.MemberStatus;
import monochrome.libri.member.domain.Role;
import monochrome.libri.member.domain.SignType;
import monochrome.libri.member.service.MemberService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.SliceImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class InquiryServiceImplTest {

    @Test
    void createInquiry_savesPendingInquiry() {
        InquiryRepository inquiryRepository = mock(InquiryRepository.class);
        MemberService memberService = mock(MemberService.class);
        InquiryServiceImpl service = new InquiryServiceImpl(inquiryRepository, memberService);

        when(memberService.getMemberById(1L)).thenReturn(Optional.of(member(1L, Role.USER)));
        when(inquiryRepository.save(any(Inquiry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.createInquiry(1L, new InquiryCreateRequestDto(" title ", " content "));

        ArgumentCaptor<Inquiry> captor = ArgumentCaptor.forClass(Inquiry.class);
        verify(inquiryRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(InquiryStatus.PENDING);
        assertThat(response.title()).isEqualTo("title");
    }

    @Test
    void getInquiry_deniesOtherUser() {
        InquiryRepository inquiryRepository = mock(InquiryRepository.class);
        MemberService memberService = mock(MemberService.class);
        InquiryServiceImpl service = new InquiryServiceImpl(inquiryRepository, memberService);

        Member owner = member(1L, Role.USER);
        Member other = member(2L, Role.USER);
        Inquiry inquiry = Inquiry.builder()
                .id(10L)
                .member(owner)
                .title("title")
                .content("content")
                .status(InquiryStatus.PENDING)
                .build();

        when(memberService.getMemberById(2L)).thenReturn(Optional.of(other));
        when(inquiryRepository.findById(10L)).thenReturn(Optional.of(inquiry));

        assertThatThrownBy(() -> service.getInquiry(10L, 2L))
                .isInstanceOf(LibriException.class);
    }

    @Test
    void answerInquiry_requiresAdminAndMarksAnswered() {
        InquiryRepository inquiryRepository = mock(InquiryRepository.class);
        MemberService memberService = mock(MemberService.class);
        InquiryServiceImpl service = new InquiryServiceImpl(inquiryRepository, memberService);

        Member owner = member(1L, Role.USER);
        Member admin = member(99L, Role.ADMIN);
        Inquiry inquiry = Inquiry.builder()
                .id(10L)
                .member(owner)
                .title("title")
                .content("content")
                .status(InquiryStatus.PENDING)
                .build();

        when(memberService.getMemberById(99L)).thenReturn(Optional.of(admin));
        when(inquiryRepository.findById(10L)).thenReturn(Optional.of(inquiry));

        var response = service.answerInquiry(99L, 10L, new InquiryAnswerRequestDto(" answer "));

        assertThat(response.status()).isEqualTo(InquiryStatus.ANSWERED);
        assertThat(response.answerContent()).isEqualTo("answer");
    }

    @Test
    void getAllInquiries_returnsContentForAdmin() {
        InquiryRepository inquiryRepository = mock(InquiryRepository.class);
        MemberService memberService = mock(MemberService.class);
        InquiryServiceImpl service = new InquiryServiceImpl(inquiryRepository, memberService);

        Member owner = member(1L, Role.USER);
        Member admin = member(99L, Role.ADMIN);
        Inquiry inquiry = Inquiry.builder()
                .id(10L)
                .member(owner)
                .title("title")
                .content("content")
                .status(InquiryStatus.PENDING)
                .build();

        when(memberService.getMemberById(99L)).thenReturn(Optional.of(admin));
        when(inquiryRepository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(inquiry), PageRequest.of(0, 20), 1));
        when(inquiryRepository.count()).thenReturn(1L);

        var response = service.getAllInquiries(99L, PageRequest.of(0, 20));

        assertThat(response.totalCount()).isEqualTo(1L);
        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).memberId()).isEqualTo(1L);
        assertThat(response.content().get(0).contentPreview()).isEqualTo("content");
    }

    @Test
    void getAllInquiries_summarizesLongContent() {
        InquiryRepository inquiryRepository = mock(InquiryRepository.class);
        MemberService memberService = mock(MemberService.class);
        InquiryServiceImpl service = new InquiryServiceImpl(inquiryRepository, memberService);

        Member owner = member(1L, Role.USER);
        Member admin = member(99L, Role.ADMIN);
        Inquiry inquiry = Inquiry.builder()
                .id(10L)
                .member(owner)
                .title("title")
                .content("0123456789012345678901234567890123456789EXTRA")
                .status(InquiryStatus.PENDING)
                .build();

        when(memberService.getMemberById(99L)).thenReturn(Optional.of(admin));
        when(inquiryRepository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(inquiry), PageRequest.of(0, 20), 1));
        when(inquiryRepository.count()).thenReturn(1L);

        var response = service.getAllInquiries(99L, PageRequest.of(0, 20));

        assertThat(response.content().get(0).contentPreview()).isEqualTo("0123456789012345678901234567890123456789...");
    }

    @Test
    void updateInquiryStatus_changesStatus() {
        InquiryRepository inquiryRepository = mock(InquiryRepository.class);
        MemberService memberService = mock(MemberService.class);
        InquiryServiceImpl service = new InquiryServiceImpl(inquiryRepository, memberService);

        Member owner = member(1L, Role.USER);
        Member admin = member(99L, Role.ADMIN);
        Inquiry inquiry = Inquiry.builder()
                .id(10L)
                .member(owner)
                .title("title")
                .content("content")
                .status(InquiryStatus.PENDING)
                .build();

        when(memberService.getMemberById(99L)).thenReturn(Optional.of(admin));
        when(inquiryRepository.findById(10L)).thenReturn(Optional.of(inquiry));

        var response = service.updateInquiryStatus(99L, 10L, new InquiryStatusUpdateRequestDto(InquiryStatus.CLOSED));

        assertThat(response.status()).isEqualTo(InquiryStatus.CLOSED);
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
