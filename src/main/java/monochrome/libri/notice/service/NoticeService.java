package monochrome.libri.notice.service;

import monochrome.libri.notice.dto.request.NoticeCreateRequestDto;
import monochrome.libri.notice.dto.request.NoticeUpdateRequestDto;
import monochrome.libri.notice.dto.response.NoticeDetailResponseDto;
import monochrome.libri.notice.dto.response.NoticeListResponseDto;
import org.springframework.data.domain.Pageable;

public interface NoticeService {
    NoticeListResponseDto getPublishedNotices(Pageable pageable);
    NoticeDetailResponseDto getPublishedNotice(long noticeId);
    NoticeDetailResponseDto createNotice(long memberId, NoticeCreateRequestDto request);
    NoticeDetailResponseDto updateNotice(long memberId, long noticeId, NoticeUpdateRequestDto request);
    void deleteNotice(long memberId, long noticeId);
}
