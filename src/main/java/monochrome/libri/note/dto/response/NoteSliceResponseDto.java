package monochrome.libri.note.dto.response;

import java.util.List;

public record NoteSliceResponseDto(
        List<NoteSummaryResponseDto> content,
        boolean hasNext,
        int page,
        int size
) {
}
