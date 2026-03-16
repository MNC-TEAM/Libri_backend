package monochrome.libri.book.dto.request;

import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record BookDirectUpdateRequestDto(
        @Size(max = 70, message = "도서 제목은 70자 이하여야 합니다.")
        String title,
        @Size(max = 25, message = "저자는 25자 이하여야 합니다.")
        String author,
        @Size(max = 15, message = "출판사는 15자 이하여야 합니다.")
        String publisher,
        @Size(max = 15, message = "ISBN은 15자 이하여야 합니다.")
        String isbn,
        Integer totalPage,
        @Size(max = 255, message = "표지 URL은 255자 이하여야 합니다.")
        String coverUrl,
        @Size(max = 1200, message = "도서 소개는 1200자 이하여야 합니다.")
        String introduction,
        LocalDate releaseDate,
        @Size(max = 255, message = "구매 링크는 255자 이하여야 합니다.")
        String salePageUrl
) {
}
