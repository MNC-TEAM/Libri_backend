package monochrome.libri.book.dto.response;

import monochrome.libri.book.domain.Book;

import java.time.LocalDate;

public record BookResponseDto(
        Long id,
        String title,
        String author,
        String publisher,
        String isbn,
        String coverImageUrl,
        String introduction,
        LocalDate releaseDate,
        int totalPage,
        String salePageUrl
) {
    public static BookResponseDto from(Book book) {
        if(book == null) return null;

        return new BookResponseDto(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getPublisher(),
                book.getIsbn(),
                book.getCoverImageUrl(),
                book.getIntroduction(),
                book.getReleaseDate(),
                book.getTotalPage(),
                book.getSalePageUrl()
        );
    }
}
