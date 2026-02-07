package monochrome.libri.book.dto.response;

import java.util.List;

public record BookSliceResponseDto(
        List<BookResponseDto> content,
        boolean hasNext,
        int page,
        int size
) {
}
