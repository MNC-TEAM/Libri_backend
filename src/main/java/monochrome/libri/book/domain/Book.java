package monochrome.libri.book.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Book {
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
}
