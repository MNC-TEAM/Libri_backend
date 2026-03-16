package monochrome.libri.search.dto.response;

import java.util.List;

public record RecentSearchListResponseDto(
        long totalCount,
        List<RecentSearchItemResponseDto> content,
        boolean hasNext,
        int page,
        int size
) {
    public static RecentSearchListResponseDto empty(int page, int size) {
        return new RecentSearchListResponseDto(0, List.of(), false, page, size);
    }
}
