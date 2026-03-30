package monochrome.libri.shelf.dto.response;

import monochrome.libri.shelf.domain.ShelfStatus;

import java.time.LocalDate;

public record ShelfDetailResponseDto(
        BookInfo book,
        ReadingInfo reading
) {
    public record BookInfo(
            Long bookId,
            String title,
            String author,
            String publisher,
            String coverUrl,
            java.time.LocalDate releaseDate
    ) {
    }

    public record ReadingInfo(
            ShelfStatus status,
            LocalDate startDate,
            LocalDate endDate,
            int progressPercent,
            int reviewCount,
            int readingDays
    ) {
    }
}
