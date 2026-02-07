package monochrome.libri.shelf.dto.request;

import jakarta.validation.constraints.NotNull;
import monochrome.libri.shelf.domain.ShelfProgressType;
import monochrome.libri.shelf.domain.ShelfStatus;

import java.time.LocalDate;

public record ShelfCreateRequestDto(
        @NotNull
        Long bookId,
        @NotNull
        ShelfStatus status,
        @NotNull
        ShelfProgressType progressType,
        @NotNull
        Integer progressValue,
        LocalDate startDate,
        LocalDate endDate
) {
}
