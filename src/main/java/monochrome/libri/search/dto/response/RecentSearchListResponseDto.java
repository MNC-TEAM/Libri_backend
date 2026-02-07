package monochrome.libri.search.dto.response;

import java.util.List;

public record RecentSearchListResponseDto(
        long totalCount,
        List<RecentSearchItemResponseDto> content,
        boolean hasNext,
        int page,
        int size
) {
}
