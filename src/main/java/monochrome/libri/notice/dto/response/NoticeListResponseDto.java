package monochrome.libri.notice.dto.response;

import java.util.List;

public record NoticeListResponseDto(
        List<NoticeListItemResponseDto> content,
        boolean hasNext,
        int page,
        int size
) {
}
