package monochrome.libri.book.service.impl;

import monochrome.libri.book.config.AladinProperties;
import monochrome.libri.book.domain.Book;
import monochrome.libri.book.integration.AladinBookItem;
import monochrome.libri.book.integration.AladinBookSearchClient;
import monochrome.libri.book.repository.BookRepository;
import monochrome.libri.book.service.AladinBookCatalogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class AladinBookCatalogServiceImpl implements AladinBookCatalogService {

    private static final int TITLE_MAX_LENGTH = 70;
    private static final int AUTHOR_MAX_LENGTH = 25;
    private static final int INTRODUCTION_MAX_LENGTH = 1200;
    private static final int PUBLISHER_MAX_LENGTH = 15;
    private static final int ISBN_MAX_LENGTH = 30;
    private static final int URL_MAX_LENGTH = 255;

    private final BookRepository bookRepository;
    private final AladinBookSearchClient aladinBookSearchClient;
    private final AladinProperties aladinProperties;

    public AladinBookCatalogServiceImpl(
            BookRepository bookRepository,
            AladinBookSearchClient aladinBookSearchClient,
            AladinProperties aladinProperties
    ) {
        this.bookRepository = bookRepository;
        this.aladinBookSearchClient = aladinBookSearchClient;
        this.aladinProperties = aladinProperties;
    }

    @Override
    public boolean isConfigured() {
        return aladinProperties.isConfigured();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<Book> fetchAndUpsertByIsbn(String isbn) {
        if (!isConfigured()) {
            return Optional.empty();
        }
        return aladinBookSearchClient.lookupByIsbn(isbn)
                .map(this::upsertExternalBook);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<Book> fetchAndUpsertByKeyword(String keyword, int start, int maxResults) {
        if (!isConfigured()) {
            return List.of();
        }
        List<AladinBookItem> items = aladinBookSearchClient.searchByKeyword(
                keyword,
                start,
                Math.max(1, Math.min(maxResults, aladinProperties.maxResults()))
        );
        return upsertAll(items);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<Book> fetchAndUpsertBestsellers(int maxResults) {
        if (!isConfigured()) {
            return List.of();
        }
        List<AladinBookItem> items = aladinBookSearchClient.fetchBestsellers(
                Math.max(1, Math.min(maxResults, aladinProperties.maxResults()))
        );
        return upsertAll(items);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void enrichBookDetailIfNeeded(Book book) {
        if (!isConfigured() || book == null) {
            return;
        }
        if (book.getRegisteredByMember() != null) {
            return;
        }
        String isbn = normalizeIsbnValue(book.getIsbn());
        if (isbn == null || isbn.isBlank()) {
            return;
        }
        if (!needsExternalEnrichment(book)) {
            return;
        }

        aladinBookSearchClient.lookupByIsbn(isbn).ifPresent(item -> {
            book.updateFromExternalSource(
                    normalizeTitle(item.title()),
                    normalizeAuthor(item.author()),
                    normalizePublisher(item.publisher()),
                    normalizeIsbnValue(item.resolvedIsbn()),
                    item.totalPage() > 0 ? item.totalPage() : null,
                    normalizeUrl(item.coverImageUrl()),
                    normalizeIntroduction(item.introduction()),
                    item.releaseDate(),
                    normalizeUrl(item.salePageUrl())
            );
            bookRepository.save(book);
        });
    }

    private List<Book> upsertAll(List<AladinBookItem> items) {
        if (items.isEmpty()) {
            return List.of();
        }
        List<Book> books = new ArrayList<>();
        for (AladinBookItem item : items) {
            books.add(upsertExternalBook(item));
        }
        return books;
    }

    private Book upsertExternalBook(AladinBookItem item) {
        Optional<Book> existing = findExistingExternalBook(item);
        if (existing.isPresent()) {
            Book book = existing.get();
            if (book.getRegisteredByMember() != null) {
                return book;
            }
            book.updateFromExternalSource(
                    normalizeTitle(item.title()),
                    normalizeAuthor(item.author()),
                    normalizePublisher(item.publisher()),
                    normalizeIsbnValue(item.resolvedIsbn()),
                    item.totalPage() > 0 ? item.totalPage() : null,
                    normalizeUrl(item.coverImageUrl()),
                    normalizeIntroduction(item.introduction()),
                    item.releaseDate(),
                    normalizeUrl(item.salePageUrl())
            );
            return bookRepository.save(book);
        }

        Book book = Book.builder()
                .title(normalizeTitle(item.title()))
                .author(normalizeAuthor(item.author()))
                .publisher(normalizePublisher(item.publisher()))
                .isbn(normalizeIsbnValue(item.resolvedIsbn()))
                .totalPage(Math.max(item.totalPage(), 0))
                .coverImageUrl(normalizeUrl(item.coverImageUrl()))
                .introduction(normalizeIntroduction(item.introduction()))
                .releaseDate(item.releaseDate())
                .salePageUrl(normalizeUrl(item.salePageUrl()))
                .build();
        return bookRepository.save(book);
    }

    private Optional<Book> findExistingExternalBook(AladinBookItem item) {
        String resolvedIsbn = normalizeIsbnValue(item.resolvedIsbn());
        if (resolvedIsbn != null) {
            Optional<Book> byIsbn = bookRepository.findByIsbn(resolvedIsbn);
            if (byIsbn.isPresent()) {
                return byIsbn;
            }
        }

        String title = normalizeTitle(item.title());
        String author = normalizeAuthor(item.author());
        String publisher = normalizePublisher(item.publisher());
        if (title != null && author != null && publisher != null) {
            Optional<Book> byTitleAuthorPublisher = bookRepository.findFirstByTitleAndAuthorAndPublisher(title, author, publisher);
            if (byTitleAuthorPublisher.isPresent()) {
                return byTitleAuthorPublisher;
            }
        }
        if (title != null && author != null) {
            return bookRepository.findFirstByTitleAndAuthor(title, author);
        }
        return Optional.empty();
    }

    private boolean needsExternalEnrichment(Book book) {
        return book.getIntroduction() == null || book.getIntroduction().isBlank()
                || book.getTotalPage() <= 0
                || book.getCoverImageUrl() == null || book.getCoverImageUrl().isBlank()
                || book.getReleaseDate() == null
                || book.getSalePageUrl() == null || book.getSalePageUrl().isBlank();
    }

    private String normalizeTitle(String value) {
        return limitLength(trimToNull(value), TITLE_MAX_LENGTH);
    }

    private String normalizeAuthor(String value) {
        return limitLength(trimToNull(value), AUTHOR_MAX_LENGTH);
    }

    private String normalizePublisher(String value) {
        return limitLength(trimToNull(value), PUBLISHER_MAX_LENGTH);
    }

    private String normalizeIntroduction(String value) {
        return limitLength(trimToNull(value), INTRODUCTION_MAX_LENGTH);
    }

    private String normalizeUrl(String value) {
        return limitLength(trimToNull(value), URL_MAX_LENGTH);
    }

    private String normalizeIsbnValue(String value) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            return null;
        }
        return limitLength(normalized.replaceAll("[\\s-]", ""), ISBN_MAX_LENGTH);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String limitLength(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
