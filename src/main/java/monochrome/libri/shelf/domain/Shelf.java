package monochrome.libri.shelf.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import monochrome.libri.book.domain.Book;
import monochrome.libri.global.domain.AuditableEntity;
import monochrome.libri.member.domain.Member;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(
        name = "shelf",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_shelf_member_book", columnNames = {"member_id", "book_id"})
        }
)
public class Shelf extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shelf_id")
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ShelfStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ShelfProgressType progressType;

    @Column(nullable = false)
    private int progressValue;

    private LocalDate startDate;

    private LocalDate endDate;

    @Column
    private Integer currentPage;

    public Shelf updateStatus(ShelfStatus status) {
        this.status = status;
        return this;
    }

    public void updateReadingInfo(ShelfStatus status, LocalDate startDate, LocalDate endDate, Integer currentPage) {
        if (status != null) {
            this.status = status;
        }
        if (startDate != null) {
            this.startDate = startDate;
        }
        if (endDate != null) {
            this.endDate = endDate;
        }
        if (currentPage != null) {
            this.currentPage = currentPage;
        }
    }

    public void updateProgress(ShelfProgressType progressType, Integer progressValue, Integer currentPage) {
        if (progressType != null) {
            this.progressType = progressType;
        }
        if (progressValue != null) {
            this.progressValue = progressValue;
        }
        this.currentPage = currentPage;
    }
}
