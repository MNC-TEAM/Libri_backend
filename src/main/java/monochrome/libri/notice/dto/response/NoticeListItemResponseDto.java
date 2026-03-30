package monochrome.libri.notice.dto.response;

import java.time.LocalDateTime;

public record NoticeListItemResponseDto(
        long noticeId,
        String title,
        boolean pinned,
        LocalDateTime createdAt
) {
}
