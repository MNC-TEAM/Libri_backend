package monochrome.libri.review.dto.response;

import java.util.List;

public record MyReviewListResponseDto(
        long totalCount,
        List<MyReviewItemResponseDto> content,
        boolean hasNext,
        int page,
        int size
) {
}
