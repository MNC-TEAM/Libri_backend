package monochrome.libri.review.dto.response;

import java.util.List;

public record MyReviewListResponseDto(
        long totalCount,
        List<MyReviewItemResponseDto> content,
        boolean hasNext,
        int page,
        int size
) {
    public static MyReviewListResponseDto empty(int page, int size) {
        return new MyReviewListResponseDto(0, List.of(), false, page, size);
    }
}
