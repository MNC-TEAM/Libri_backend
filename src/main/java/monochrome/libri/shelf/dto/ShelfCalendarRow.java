package monochrome.libri.shelf.dto;

import monochrome.libri.shelf.domain.ShelfStatus;

import java.time.LocalDate;

public record ShelfCalendarRow(
        Long shelfId,
        Long bookId,
        String title,
        String coverUrl,
        LocalDate startDate,
        LocalDate endDate,
        ShelfStatus status
) {
}
