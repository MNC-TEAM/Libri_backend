package monochrome.libri.note.dto.response;

import java.util.List;

public record NoteListResponseDto(
        long totalCount,
        List<NoteListItemResponseDto> content,
        boolean hasNext,
        int page,
        int size
) {
    public static NoteListResponseDto empty(int page, int size) {
        return new NoteListResponseDto(0, List.of(), false, page, size);
    }
}
