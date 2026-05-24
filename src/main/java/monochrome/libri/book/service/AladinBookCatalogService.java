package monochrome.libri.book.service;

import monochrome.libri.book.domain.Book;

import java.util.List;
import java.util.Optional;

public interface AladinBookCatalogService {
    boolean isConfigured();
    Optional<Book> fetchAndUpsertByIsbn(String isbn);
    List<Book> fetchAndUpsertByKeyword(String keyword, int start, int maxResults);
    List<Book> fetchAndUpsertBestsellers(int maxResults);
    void enrichBookDetailIfNeeded(Book book);
}
