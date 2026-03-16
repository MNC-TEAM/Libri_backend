package monochrome.libri.note.dto.response;

import java.util.List;

public record NoteBookmarkListResponseDto(
        long totalCount,
        List<NoteBookmarkItemResponseDto> content,
        boolean hasNext,
        int page,
        int size
) {
    public static NoteBookmarkListResponseDto empty(int page, int size) {
        return new NoteBookmarkListResponseDto(0, List.of(), false, page, size);
    }
}
