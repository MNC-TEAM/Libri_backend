package monochrome.libri.shelf.dto.request;

import monochrome.libri.shelf.domain.ShelfProgressType;
import monochrome.libri.shelf.domain.ShelfStatus;

import java.time.LocalDate;

public record ShelfUpdateRequestDto(
        ShelfStatus status,
        LocalDate startDate,
        LocalDate endDate,
        ShelfProgressType progressType,
        Integer progressValue
) {
}
