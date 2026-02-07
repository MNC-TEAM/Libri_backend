package monochrome.libri.note.dto.response;

import java.util.List;

public record NoteBookmarkListResponseDto(
        long totalCount,
        List<NoteBookmarkItemResponseDto> content,
        boolean hasNext,
        int page,
        int size
) {
}
