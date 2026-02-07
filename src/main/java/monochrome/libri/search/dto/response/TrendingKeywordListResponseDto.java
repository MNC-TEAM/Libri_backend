package monochrome.libri.search.dto.response;

import java.util.List;

public record TrendingKeywordListResponseDto(
        List<TrendingKeywordResponseDto> content
) {
}
