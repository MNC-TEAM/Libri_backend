package monochrome.libri.book.domain;

import jakarta.persistence.*;
import lombok.*;
import monochrome.libri.global.domain.AuditableEntity;
import monochrome.libri.member.domain.Member;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registered_by_member_id")
    private Member registeredByMember;

    private static final Pattern ISBN_CLEANUP = Pattern.compile("[\\s-]");

    @PrePersist
    @PreUpdate
    void normalizeIsbn() {
        if (isbn != null) {
            isbn = ISBN_CLEANUP.matcher(isbn).replaceAll("");
        }
    }

    public void updateDirectBook(
            String title,
            String author,
            String publisher,
            String isbn,
            Integer totalPage,
            String coverImageUrl,
            String introduction,
            LocalDate releaseDate,
            String salePageUrl
    ) {
        if (title != null) {
            this.title = title;
        }
        if (author != null) {
            this.author = author;
        }
        if (publisher != null) {
            this.publisher = publisher;
        }
        if (isbn != null) {
            this.isbn = isbn;
        }
        if (totalPage != null) {
            this.totalPage = totalPage;
        }
        if (coverImageUrl != null) {
            this.coverImageUrl = coverImageUrl;
        }
        if (introduction != null) {
            this.introduction = introduction;
        }
        if (releaseDate != null) {
            this.releaseDate = releaseDate;
        }
        if (salePageUrl != null) {
            this.salePageUrl = salePageUrl;
        }
    }
}
