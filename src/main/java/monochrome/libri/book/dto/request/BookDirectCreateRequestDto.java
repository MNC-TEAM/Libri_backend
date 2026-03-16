package monochrome.libri.book.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import monochrome.libri.shelf.domain.ShelfStatus;

public record BookDirectCreateRequestDto(
        @NotBlank(message = "도서 제목은 필수입니다.")
        @Size(max = 70, message = "도서 제목은 70자 이하여야 합니다.")
        String title,
        @NotBlank(message = "저자는 필수입니다.")
        @Size(max = 25, message = "저자는 25자 이하여야 합니다.")
        String author,
        @Size(max = 120, message = "출판사는 120자 이하여야 합니다.")
        String publisher,
        @Size(max = 15, message = "ISBN은 15자 이하여야 합니다.")
        String isbn,
        Integer totalPage,
        @NotNull(message = "서재 상태는 필수입니다.")
        ShelfStatus status,
        String coverUrl
) {
}
