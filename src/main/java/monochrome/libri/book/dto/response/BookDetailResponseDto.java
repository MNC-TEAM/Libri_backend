package monochrome.libri.book.dto.response;

import monochrome.libri.review.dto.response.ReviewStatsResponseDto;
import monochrome.libri.review.dto.response.ReviewSummaryResponseDto;
import monochrome.libri.shelf.domain.ShelfStatus;

import java.time.LocalDate;
import java.util.List;

public record BookDetailResponseDto(
        Long bookId,
        String title,
        String author,
        String publisher,
        LocalDate releaseDate,
        String coverUrl,
        String introduction,
        ShelfInfo shelf,
        ReviewStatsResponseDto reviewStats,
        List<ReviewSummaryResponseDto> reviews
) {
    public record ShelfInfo(
            Long shelfId,
            ShelfStatus status
    ) {
    }
}
