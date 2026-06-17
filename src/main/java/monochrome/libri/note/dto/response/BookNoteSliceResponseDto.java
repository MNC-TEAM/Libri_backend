package monochrome.libri.note.dto.response;

import java.util.List;

public record BookNoteSliceResponseDto(
        List<BookNoteItemResponseDto> content,
        boolean hasNext,
        int page,
        int size
) {
}
