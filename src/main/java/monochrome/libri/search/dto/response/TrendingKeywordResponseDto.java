package monochrome.libri.search.dto.response;

public record TrendingKeywordResponseDto(
        TrendingBookSummaryResponseDto book,
        long count
) {
}
