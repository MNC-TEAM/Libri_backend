package monochrome.libri.shelf.dto.request;

import jakarta.validation.constraints.NotNull;
import monochrome.libri.shelf.domain.ShelfProgressType;
import monochrome.libri.shelf.domain.ShelfStatus;

import java.time.LocalDate;

public record ShelfCreateRequestDto(
        @NotNull(message = "도서 ID는 필수입니다.")
        Long bookId,
        @NotNull(message = "서재 상태는 필수입니다.")
        ShelfStatus status,
        @NotNull(message = "진행도 타입은 필수입니다.")
        ShelfProgressType progressType,
        @NotNull(message = "진행도 값은 필수입니다.")
        Integer progressValue,
        LocalDate startDate,
        LocalDate endDate
) {
}
