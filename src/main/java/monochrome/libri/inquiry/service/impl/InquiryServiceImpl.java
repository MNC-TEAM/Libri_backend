package monochrome.libri.inquiry.service.impl;

import monochrome.libri.global.exception.ErrorCode;
import monochrome.libri.global.exception.LibriException;
import monochrome.libri.inquiry.domain.Inquiry;
import monochrome.libri.inquiry.domain.InquiryStatus;
import monochrome.libri.inquiry.dto.request.InquiryAnswerRequestDto;
import monochrome.libri.inquiry.dto.request.InquiryCreateRequestDto;
import monochrome.libri.inquiry.dto.request.InquiryStatusUpdateRequestDto;
import monochrome.libri.inquiry.dto.response.InquiryDetailResponseDto;
import monochrome.libri.inquiry.dto.response.InquiryListItemResponseDto;
import monochrome.libri.inquiry.dto.response.InquiryListResponseDto;
import monochrome.libri.inquiry.repository.InquiryRepository;
import monochrome.libri.inquiry.service.InquiryService;
import monochrome.libri.member.domain.Member;
import monochrome.libri.member.domain.Role;
import monochrome.libri.member.service.MemberService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class InquiryServiceImpl implements InquiryService {
    private static final int INQUIRY_PREVIEW_LENGTH = 40;

    private final InquiryRepository inquiryRepository;
    private final MemberService memberService;

    public InquiryServiceImpl(InquiryRepository inquiryRepository, MemberService memberService) {
        this.inquiryRepository = inquiryRepository;
        this.memberService = memberService;
    }

    @Override
    @Transactional
    public InquiryDetailResponseDto createInquiry(long memberId, InquiryCreateRequestDto request) {
        Member member = getMemberOrThrow(memberId);
        if (request == null || request.title() == null || request.title().isBlank()
                || request.content() == null || request.content().isBlank()) {
            throw new LibriException(ErrorCode.INVALID_INPUT_VALUE);
        }

        Inquiry inquiry = Inquiry.builder()
                .member(member)
                .title(request.title().trim())
                .content(request.content().trim())
                .status(InquiryStatus.PENDING)
                .build();

        return InquiryDetailResponseDto.from(inquiryRepository.save(inquiry));
    }

    @Override
    public InquiryListResponseDto getMyInquiries(long memberId, Pageable pageable) {
        if (memberId <= 0) {
            throw new LibriException(ErrorCode.AUTHENTICATION_FAILED);
        }

        Slice<Inquiry> slice = inquiryRepository.findByMemberIdOrderByCreatedDateDesc(memberId, pageable);
        List<InquiryListItemResponseDto> content = toListItems(slice.getContent());
        long totalCount = inquiryRepository.countByMemberId(memberId);
        return new InquiryListResponseDto(totalCount, content, slice.hasNext(), slice.getNumber(), slice.getSize());
    }

    @Override
    public InquiryDetailResponseDto getInquiry(long inquiryId, long memberId) {
        if (memberId <= 0) {
            throw new LibriException(ErrorCode.AUTHENTICATION_FAILED);
        }
        Member actor = getMemberOrThrow(memberId);
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new LibriException(ErrorCode.INQUIRY_NOT_FOUND));
        if (actor.getRole() != Role.ADMIN && inquiry.getMember().getId() != memberId) {
            throw new LibriException(ErrorCode.ACCESS_DENIED);
        }
        return InquiryDetailResponseDto.from(inquiry);
    }

    @Override
    public InquiryListResponseDto getAllInquiries(long memberId, Pageable pageable) {
        getAdminMember(memberId);
        Slice<Inquiry> slice = inquiryRepository.findAll(pageable);
        List<InquiryListItemResponseDto> content = toListItems(slice.getContent());
        return new InquiryListResponseDto(inquiryRepository.count(), content, slice.hasNext(), slice.getNumber(), slice.getSize());
    }

    @Override
    @Transactional
    public InquiryDetailResponseDto answerInquiry(long memberId, long inquiryId, InquiryAnswerRequestDto request) {
        getAdminMember(memberId);
        if (request == null || request.answerContent() == null || request.answerContent().isBlank()) {
            throw new LibriException(ErrorCode.INVALID_INPUT_VALUE);
        }

        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new LibriException(ErrorCode.INQUIRY_NOT_FOUND));
        inquiry.answer(request.answerContent().trim(), LocalDateTime.now());
        return InquiryDetailResponseDto.from(inquiry);
    }

    @Override
    @Transactional
    public InquiryDetailResponseDto updateInquiryStatus(long memberId, long inquiryId, InquiryStatusUpdateRequestDto request) {
        getAdminMember(memberId);
        if (request == null || request.status() == null) {
            throw new LibriException(ErrorCode.INVALID_INPUT_VALUE);
        }

        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new LibriException(ErrorCode.INQUIRY_NOT_FOUND));
        inquiry.updateStatus(request.status());
        return InquiryDetailResponseDto.from(inquiry);
    }

    private List<InquiryListItemResponseDto> toListItems(List<Inquiry> inquiries) {
        return inquiries.stream()
                .map(inquiry -> new InquiryListItemResponseDto(
                        inquiry.getId(),
                        inquiry.getMember().getId(),
                        inquiry.getMember().getNickname(),
                        inquiry.getTitle(),
                        summarizeContent(inquiry.getContent()),
                        inquiry.getStatus(),
                        inquiry.getAnsweredAt(),
                        inquiry.getCreatedDate()
                ))
                .toList();
    }

    private String summarizeContent(String content) {
        if (content == null) {
            return "";
        }

        String normalized = content.trim().replaceAll("\\s+", " ");
        if (normalized.length() <= INQUIRY_PREVIEW_LENGTH) {
            return normalized;
        }
        return normalized.substring(0, INQUIRY_PREVIEW_LENGTH) + "...";
    }

    private Member getMemberOrThrow(long memberId) {
        if (memberId <= 0) {
            throw new LibriException(ErrorCode.AUTHENTICATION_FAILED);
        }
        return memberService.getMemberById(memberId)
                .orElseThrow(() -> new LibriException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private Member getAdminMember(long memberId) {
        Member member = getMemberOrThrow(memberId);
        if (member.getRole() != Role.ADMIN) {
            throw new LibriException(ErrorCode.ACCESS_DENIED);
        }
        return member;
    }
}
