package monochrome.libri.note.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import monochrome.libri.global.domain.AuditableEntity;
import monochrome.libri.member.domain.Member;
import monochrome.libri.shelf.domain.Shelf;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "note")
public class Note extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "note_id")
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shelf_id", nullable = false)
    private Shelf shelf;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false, length = 2000)
    private String content;

    @Column(nullable = false)
    private boolean secret;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NoteProgressType progressType;

    @Column(nullable = false)
    private int progressValue;

    @Column(nullable = false)
    private int likeCount;

    @Column(nullable = false)
    private int bookmarkCount;

    public Note update(String content, Boolean secret) {
        if (content != null) {
            this.content = content;
        }
        if (secret != null) {
            this.secret = secret;
        }
        return this;
    }

    public void updateProgress(NoteProgressType progressType, Integer progressValue) {
        if (progressType != null) {
            this.progressType = progressType;
        }
        if (progressValue != null) {
            this.progressValue = progressValue;
        }
    }

    public void updateShelf(Shelf shelf) {
        if (shelf != null) {
            this.shelf = shelf;
        }
    }

    public void increaseLikeCount() {
        this.likeCount += 1;
    }

    public void decreaseLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount -= 1;
        }
    }

    public void increaseBookmarkCount() {
        this.bookmarkCount += 1;
    }

    public void decreaseBookmarkCount() {
        if (this.bookmarkCount > 0) {
            this.bookmarkCount -= 1;
        }
    }
}
