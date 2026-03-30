package monochrome.libri.shelf.dto.response;

import monochrome.libri.shelf.domain.ShelfStatus;

import java.time.LocalDate;

public record ShelfListItemResponseDto(
        long shelfId,
        long bookId,
        String title,
        String author,
        String publisher,
        String coverUrl,
        ShelfStatus status,
        int progressPercent,
        LocalDate startDate,
        LocalDate endDate
) {
}
