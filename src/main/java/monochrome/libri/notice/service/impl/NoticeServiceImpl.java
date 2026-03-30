package monochrome.libri.notice.service.impl;

import monochrome.libri.global.exception.ErrorCode;
import monochrome.libri.global.exception.LibriException;
import monochrome.libri.member.domain.Member;
import monochrome.libri.member.domain.Role;
import monochrome.libri.member.service.MemberService;
import monochrome.libri.notice.domain.Notice;
import monochrome.libri.notice.dto.request.NoticeCreateRequestDto;
import monochrome.libri.notice.dto.request.NoticeUpdateRequestDto;
import monochrome.libri.notice.dto.response.NoticeDetailResponseDto;
import monochrome.libri.notice.dto.response.NoticeListItemResponseDto;
import monochrome.libri.notice.dto.response.NoticeListResponseDto;
import monochrome.libri.notice.repository.NoticeRepository;
import monochrome.libri.notice.service.NoticeService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class NoticeServiceImpl implements NoticeService {

    private final NoticeRepository noticeRepository;
    private final MemberService memberService;

    public NoticeServiceImpl(NoticeRepository noticeRepository, MemberService memberService) {
        this.noticeRepository = noticeRepository;
        this.memberService = memberService;
    }

    @Override
    public NoticeListResponseDto getPublishedNotices(Pageable pageable) {
        Slice<Notice> slice = noticeRepository.findByPublishedTrueOrderByPinnedDescCreatedDateDesc(pageable);
        List<NoticeListItemResponseDto> content = slice.getContent().stream()
                .map(notice -> new NoticeListItemResponseDto(
                        notice.getId(),
                        notice.getTitle(),
                        notice.isPinned(),
                        notice.getCreatedDate()
                ))
                .toList();

        return new NoticeListResponseDto(content, slice.hasNext(), slice.getNumber(), slice.getSize());
    }

    @Override
    public NoticeDetailResponseDto getPublishedNotice(long noticeId) {
        Notice notice = noticeRepository.findByIdAndPublishedTrue(noticeId)
                .orElseThrow(() -> new LibriException(ErrorCode.NOTICE_NOT_FOUND));
        return NoticeDetailResponseDto.from(notice);
    }

    @Override
    @Transactional
    public NoticeDetailResponseDto createNotice(long memberId, NoticeCreateRequestDto request) {
        Member member = getAdminMember(memberId);
        if (request == null || request.title() == null || request.title().isBlank()
                || request.content() == null || request.content().isBlank()) {
            throw new LibriException(ErrorCode.INVALID_INPUT_VALUE);
        }

        Notice notice = Notice.builder()
                .title(request.title().trim())
                .content(request.content().trim())
                .pinned(Boolean.TRUE.equals(request.pinned()))
                .published(request.published() == null || request.published())
                .build();

        return NoticeDetailResponseDto.from(noticeRepository.save(notice));
    }

    @Override
    @Transactional
    public NoticeDetailResponseDto updateNotice(long memberId, long noticeId, NoticeUpdateRequestDto request) {
        getAdminMember(memberId);
        if (request == null || isEmptyUpdate(request)) {
            throw new LibriException(ErrorCode.INVALID_INPUT_VALUE);
        }

        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new LibriException(ErrorCode.NOTICE_NOT_FOUND));

        String title = request.title() == null ? null : request.title().trim();
        String content = request.content() == null ? null : request.content().trim();
        if ("".equals(title) || "".equals(content)) {
            throw new LibriException(ErrorCode.INVALID_INPUT_VALUE);
        }

        notice.update(title, content, request.pinned(), request.published());
        return NoticeDetailResponseDto.from(notice);
    }

    @Override
    @Transactional
    public void deleteNotice(long memberId, long noticeId) {
        getAdminMember(memberId);
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new LibriException(ErrorCode.NOTICE_NOT_FOUND));
        noticeRepository.delete(notice);
    }

    private boolean isEmptyUpdate(NoticeUpdateRequestDto request) {
        return request.title() == null
                && request.content() == null
                && request.pinned() == null
                && request.published() == null;
    }

    private Member getAdminMember(long memberId) {
        if (memberId <= 0) {
            throw new LibriException(ErrorCode.AUTHENTICATION_FAILED);
        }
        Member member = memberService.getMemberById(memberId)
                .orElseThrow(() -> new LibriException(ErrorCode.MEMBER_NOT_FOUND));
        if (member.getRole() != Role.ADMIN) {
            throw new LibriException(ErrorCode.ACCESS_DENIED);
        }
        return member;
    }
}
