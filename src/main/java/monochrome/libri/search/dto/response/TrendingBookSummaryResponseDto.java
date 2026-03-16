package monochrome.libri.search.dto.response;

import monochrome.libri.book.domain.Book;

public record TrendingBookSummaryResponseDto(
        Long bookId,
        String title,
        String coverUrl
) {
    public static TrendingBookSummaryResponseDto from(Book book) {
        return new TrendingBookSummaryResponseDto(
                book.getId(),
                book.getTitle(),
                book.getCoverImageUrl()
        );
    }
}
