package monochrome.libri.notice.dto.response;

import monochrome.libri.notice.domain.Notice;

import java.time.LocalDateTime;

public record NoticeDetailResponseDto(
        long noticeId,
        String title,
        String content,
        boolean pinned,
        boolean published,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static NoticeDetailResponseDto from(Notice notice) {
        return new NoticeDetailResponseDto(
                notice.getId(),
                notice.getTitle(),
                notice.getContent(),
                notice.isPinned(),
                notice.isPublished(),
                notice.getCreatedDate(),
                notice.getLastModifiedDate()
        );
    }
}
