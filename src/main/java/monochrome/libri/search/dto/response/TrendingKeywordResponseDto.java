package monochrome.libri.search.dto.response;

public record TrendingKeywordResponseDto(
        String keyword,
        long count
) {
}
