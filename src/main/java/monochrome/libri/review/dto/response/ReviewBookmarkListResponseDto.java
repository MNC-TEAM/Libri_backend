package monochrome.libri.review.dto.response;

import java.util.List;

public record ReviewBookmarkListResponseDto(
        long totalCount,
        List<ReviewBookmarkItemResponseDto> content,
        boolean hasNext,
        int page,
        int size
) {
}
