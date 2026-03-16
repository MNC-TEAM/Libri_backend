package monochrome.libri.review.dto.response;

import java.util.List;

public record ReviewBookmarkListResponseDto(
        long totalCount,
        List<ReviewBookmarkItemResponseDto> content,
        boolean hasNext,
        int page,
        int size
) {
    public static ReviewBookmarkListResponseDto empty(int page, int size) {
        return new ReviewBookmarkListResponseDto(0, List.of(), false, page, size);
    }
}
