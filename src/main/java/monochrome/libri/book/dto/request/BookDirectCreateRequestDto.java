package monochrome.libri.book.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import monochrome.libri.shelf.domain.ShelfStatus;

public record BookDirectCreateRequestDto(
        @NotBlank
        @Size(max = 70)
        String title,
        @NotBlank
        @Size(max = 25)
        String author,
        @Size(max = 15)
        String publisher,
        @Size(max = 15)
        String isbn,
        Integer totalPage,
        @NotNull
        ShelfStatus status,
        String coverUrl
) {
}
