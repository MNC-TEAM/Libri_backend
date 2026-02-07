package monochrome.libri.book.domain;

import jakarta.persistence.*;
import lombok.*;
import monochrome.libri.global.domain.AuditableEntity;

import java.time.LocalDate;
import java.util.regex.Pattern;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Book extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_id")
    private long id;

    @Column(length = 70)
    String title;

    @Column(length = 25)
    String author;

    @Column(length = 1200)
    String introduction;

    @Column(length = 15)
    String publisher;

    LocalDate releaseDate;

    int totalPage;

    @Column(length = 30)
    String isbn;

    @Column(length = 255)
    String salePageUrl;

    @Column(length = 255)
    String coverImageUrl;

    private static final Pattern ISBN_CLEANUP = Pattern.compile("[\\s-]");

    @PrePersist
    @PreUpdate
    void normalizeIsbn() {
        if (isbn != null) {
            isbn = ISBN_CLEANUP.matcher(isbn).replaceAll("");
        }
    }
}
