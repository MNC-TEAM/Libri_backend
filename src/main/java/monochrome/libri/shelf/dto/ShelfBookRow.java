package monochrome.libri.shelf.dto;

import java.time.LocalDate;

public record ShelfBookRow(
        Long shelfId,
        Long bookId,
        String title,
        String coverUrl,
        Integer totalPage,
        Integer currentPage,
        monochrome.libri.shelf.domain.ShelfProgressType progressType,
        Integer progressValue,
        LocalDate startDate
) {
}
