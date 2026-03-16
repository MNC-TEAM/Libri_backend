package monochrome.libri.shelf.dto.response;

import java.util.List;

public record ShelfListResponseDto(
        long totalCount,
        List<ShelfListItemResponseDto> content,
        boolean hasNext,
        int page,
        int size
) {
    public static ShelfListResponseDto empty(int page, int size) {
        return new ShelfListResponseDto(0, List.of(), false, page, size);
    }
}
