package monochrome.libri.book.integration;

import java.time.LocalDate;

public record AladinBookItem(
        String title,
        String author,
        String publisher,
        String isbn,
        String isbn13,
        String coverImageUrl,
        String introduction,
        LocalDate releaseDate,
        int totalPage,
        String salePageUrl
) {
    public String resolvedIsbn() {
        if (isbn13 != null && !isbn13.isBlank()) {
            return isbn13;
        }
        return isbn;
    }
}
