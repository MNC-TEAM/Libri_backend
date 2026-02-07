package monochrome.libri.note.dto.response;

import java.util.List;

public record NoteListResponseDto(
        long totalCount,
        List<NoteListItemResponseDto> content,
        boolean hasNext,
        int page,
        int size
) {
}
