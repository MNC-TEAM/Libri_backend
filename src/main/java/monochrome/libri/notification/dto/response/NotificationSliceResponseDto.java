package monochrome.libri.notification.dto.response;

import java.util.List;

public record NotificationSliceResponseDto(
        long totalCount,
        List<NotificationResponseDto> content,
        boolean hasNext,
        int page,
        int size
) {
}
