package monochrome.libri.review.dto.response;

import java.util.List;

public record ReviewSliceResponseDto(
        List<ReviewSummaryResponseDto> content,
        boolean hasNext,
        int page,
        int size
) {
    public static ReviewSliceResponseDto empty(int page, int size) {
        return new ReviewSliceResponseDto(List.of(), false, page, size);
    }
}
