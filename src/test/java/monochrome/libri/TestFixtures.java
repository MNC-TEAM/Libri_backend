package monochrome.libri;

import monochrome.libri.book.domain.Book;
import monochrome.libri.follow.domain.Follow;
import monochrome.libri.follow.domain.FollowStatus;
import monochrome.libri.member.domain.Member;
import monochrome.libri.member.domain.MemberStatus;
import monochrome.libri.member.domain.Role;
import monochrome.libri.member.domain.SignType;
import monochrome.libri.note.domain.Note;
import monochrome.libri.note.domain.NoteProgressType;
import monochrome.libri.review.domain.Review;
import monochrome.libri.review.domain.ReviewStatus;
import monochrome.libri.shelf.domain.Shelf;
import monochrome.libri.shelf.domain.ShelfProgressType;
import monochrome.libri.shelf.domain.ShelfStatus;

import java.time.LocalDate;

public final class TestFixtures {
    private TestFixtures() {
    }

    public static Member member(long id) {
        return Member.builder()
                .id(id)
                .provider(SignType.EMAIL)
                .providerUserId(null)
                .email("user" + id + "@test.com")
                .emailVerified(true)
                .nickname("nick" + id)
                .username("user" + id)
                .passwordHash("hashed" + id)
                .profilePath("/profile/" + id)
                .memberStatus(MemberStatus.ACTIVE)
                .role(Role.USER)
                .build();
    }

    public static Book book(long id, int totalPage) {
        return Book.builder()
                .id(id)
                .title("title" + id)
                .author("author" + id)
                .publisher("publisher" + id)
                .isbn("9780306406157")
                .totalPage(totalPage)
                .coverImageUrl("/cover/" + id)
                .build();
    }

    public static Shelf shelf(long id, Member member, Book book) {
        return Shelf.builder()
                .id(id)
                .member(member)
                .book(book)
                .status(ShelfStatus.READING)
                .progressType(ShelfProgressType.PAGE)
                .progressValue(0)
                .startDate(LocalDate.now())
                .build();
    }

    public static Note note(long id, Shelf shelf, Member member, boolean secret) {
        return Note.builder()
                .id(id)
                .shelf(shelf)
                .member(member)
                .content("note" + id)
                .secret(secret)
                .progressType(NoteProgressType.PAGE)
                .progressValue(1)
                .likeCount(0)
                .bookmarkCount(0)
                .build();
    }

    public static Review review(long id, Member member, Book book, int rating, String content, ReviewStatus status) {
        return Review.builder()
                .id(id)
                .member(member)
                .book(book)
                .rating(rating)
                .content(content)
                .status(status)
                .build();
    }

    public static Follow follow(Member follower, Member following, FollowStatus status) {
        return Follow.builder()
                .id(1L)
                .follower(follower)
                .following(following)
                .followStatus(status)
                .build();
    }
}
