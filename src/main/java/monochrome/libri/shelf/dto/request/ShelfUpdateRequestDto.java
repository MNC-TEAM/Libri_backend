package monochrome.libri.shelf.dto.request;

import jakarta.validation.constraints.Min;
import monochrome.libri.shelf.domain.ShelfProgressType;
import monochrome.libri.shelf.domain.ShelfStatus;

import java.time.LocalDate;

public record ShelfUpdateRequestDto(
        ShelfStatus status,
        LocalDate startDate,
        LocalDate endDate,
        ShelfProgressType progressType,
        @Min(value = 0, message = "진행도 값은 0 이상이어야 합니다.")
        Integer progressValue
) {
}
