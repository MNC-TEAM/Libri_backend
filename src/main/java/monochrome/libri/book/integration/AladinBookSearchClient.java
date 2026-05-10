package monochrome.libri.book.integration;

import java.util.List;
import java.util.Optional;

public interface AladinBookSearchClient {
    List<AladinBookItem> searchByKeyword(String keyword, int start, int maxResults);
    Optional<AladinBookItem> lookupByIsbn(String isbn);
    List<AladinBookItem> fetchBestsellers(int maxResults);
}
