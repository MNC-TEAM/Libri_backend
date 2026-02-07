package monochrome.libri.search.dto.response;

import java.time.LocalDateTime;

public record RecentSearchItemResponseDto(
        Long id,
        String keyword,
        LocalDateTime createdDate
) {
}
