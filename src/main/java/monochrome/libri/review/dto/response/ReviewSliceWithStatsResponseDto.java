package monochrome.libri.review.dto.response;

import java.util.List;

public record ReviewSliceWithStatsResponseDto(
        ReviewStatsResponseDto stats,
        List<ReviewSummaryResponseDto> content,
        boolean hasNext,
        int page,
        int size
) {
}
