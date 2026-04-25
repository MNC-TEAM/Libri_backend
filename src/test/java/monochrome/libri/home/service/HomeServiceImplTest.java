package monochrome.libri.home.service;

import monochrome.libri.TestFixtures;
import monochrome.libri.book.domain.Book;
import monochrome.libri.book.repository.BookRepository;
import monochrome.libri.follow.domain.FollowStatus;
import monochrome.libri.follow.repository.FollowRepository;
import monochrome.libri.home.dto.response.HomeResponseDto;
import monochrome.libri.home.service.impl.HomeServiceImpl;
import monochrome.libri.member.domain.Member;
import monochrome.libri.member.service.MemberService;
import monochrome.libri.shelf.domain.ShelfProgressType;
import monochrome.libri.shelf.domain.ShelfStatus;
import monochrome.libri.shelf.dto.ShelfBookRow;
import monochrome.libri.shelf.repository.ShelfCountSummary;
import monochrome.libri.shelf.repository.ShelfRepository;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HomeServiceImplTest {

    @Test
    void getHome_returnsAnonymousSummaryForNullMember() {
        MemberService memberService = mock(MemberService.class);
        FollowRepository followRepository = mock(FollowRepository.class);
        ShelfRepository shelfRepository = mock(ShelfRepository.class);
        BookRepository bookRepository = mock(BookRepository.class);
        Clock clock = Clock.fixed(Instant.parse("2024-01-10T00:00:00Z"), ZoneOffset.UTC);

        when(bookRepository.findRecommendedBooks(null, 3)).thenReturn(List.of());

        HomeServiceImpl service = new HomeServiceImpl(memberService, followRepository, shelfRepository, bookRepository, clock);

        HomeResponseDto response = service.getHome(null);

        assertThat(response.me().memberId()).isNull();
        assertThat(response.me().followerCount()).isZero();
        assertThat(response.me().followingCount()).isZero();
        assertThat(response.shelfSummary().wantToReadCount()).isEqualTo(0);
        assertThat(response.readingBooks()).isEmpty();
    }

    @Test
    void getHome_buildsReadingBooksAndCounts() {
        MemberService memberService = mock(MemberService.class);
        FollowRepository followRepository = mock(FollowRepository.class);
        ShelfRepository shelfRepository = mock(ShelfRepository.class);
        BookRepository bookRepository = mock(BookRepository.class);
        Clock clock = Clock.fixed(Instant.parse("2024-01-10T00:00:00Z"), ZoneOffset.UTC);

        Member member = TestFixtures.member(1L);
        when(memberService.getMemberById(1L)).thenReturn(Optional.of(member));
        when(followRepository.countByFollowingAndFollowStatus(member, FollowStatus.FOLLOW)).thenReturn(4L);
        when(followRepository.countByFollowerAndFollowStatus(member, FollowStatus.FOLLOW)).thenReturn(7L);
        when(shelfRepository.findCountSummaryByMemberId(1L))
                .thenReturn(new ShelfCountSummary(1, 2, 3));

        ShelfBookRow readingRow = new ShelfBookRow(
                10L,
                20L,
                "Title",
                "/cover",
                200,
                50,
                ShelfProgressType.PAGE,
                50,
                LocalDate.of(2024, 1, 8)
        );
        when(shelfRepository.findShelfBooksByStatus(eq(1L), eq(ShelfStatus.READING), eq(3)))
                .thenReturn(List.of(readingRow));
        when(shelfRepository.findShelfBooksByStatus(eq(1L), eq(ShelfStatus.WANT_TO_READ), eq(3)))
                .thenReturn(List.of());
        when(shelfRepository.findShelfBooksByStatus(eq(1L), eq(ShelfStatus.FINISHED), eq(3)))
                .thenReturn(List.of());
        when(bookRepository.findRecommendedBooks(1L, 3))
                .thenReturn(List.of(TestFixtures.book(30L, 320)));

        HomeServiceImpl service = new HomeServiceImpl(memberService, followRepository, shelfRepository, bookRepository, clock);
        HomeResponseDto response = service.getHome(1L);

        assertThat(response.me().followerCount()).isEqualTo(4);
        assertThat(response.me().followingCount()).isEqualTo(7);
        assertThat(response.me().unreadNotiCount()).isEqualTo(6);
        assertThat(response.shelfSummary().wantToReadCount()).isEqualTo(1);
        assertThat(response.readingBooks()).hasSize(1);
        assertThat(response.recommendations()).hasSize(1);
        assertThat(response.recommendations().get(0).bookId()).isEqualTo(30L);
        assertThat(response.recommendations().get(0).badgeText()).isEqualTo("리뷰 인기");
        HomeResponseDto.ReadingBook book = response.readingBooks().get(0);
        assertThat(book.progressPercent()).isEqualTo(25);
        assertThat(book.readingDays()).isEqualTo(3);
    }

    @Test
    void getHome_usesProgressValueWhenPageTypeAndCurrentPageIsNull() {
        MemberService memberService = mock(MemberService.class);
        FollowRepository followRepository = mock(FollowRepository.class);
        ShelfRepository shelfRepository = mock(ShelfRepository.class);
        BookRepository bookRepository = mock(BookRepository.class);
        Clock clock = Clock.fixed(Instant.parse("2024-01-10T00:00:00Z"), ZoneOffset.UTC);

        Member member = TestFixtures.member(1L);
        when(memberService.getMemberById(1L)).thenReturn(Optional.of(member));
        when(followRepository.countByFollowingAndFollowStatus(member, FollowStatus.FOLLOW)).thenReturn(0L);
        when(followRepository.countByFollowerAndFollowStatus(member, FollowStatus.FOLLOW)).thenReturn(0L);
        when(shelfRepository.findCountSummaryByMemberId(1L))
                .thenReturn(new ShelfCountSummary(0, 1, 0));

        ShelfBookRow readingRow = new ShelfBookRow(
                10L,
                20L,
                "Title",
                "/cover",
                200,
                null,
                ShelfProgressType.PAGE,
                50,
                LocalDate.of(2024, 1, 8)
        );
        when(shelfRepository.findShelfBooksByStatus(eq(1L), eq(ShelfStatus.READING), eq(3)))
                .thenReturn(List.of(readingRow));
        when(shelfRepository.findShelfBooksByStatus(eq(1L), eq(ShelfStatus.WANT_TO_READ), eq(3)))
                .thenReturn(List.of());
        when(shelfRepository.findShelfBooksByStatus(eq(1L), eq(ShelfStatus.FINISHED), eq(3)))
                .thenReturn(List.of());
        when(bookRepository.findRecommendedBooks(1L, 3)).thenReturn(List.of());

        HomeServiceImpl service = new HomeServiceImpl(memberService, followRepository, shelfRepository, bookRepository, clock);
        HomeResponseDto response = service.getHome(1L);

        assertThat(response.readingBooks()).hasSize(1);
        assertThat(response.readingBooks().get(0).progressPercent()).isEqualTo(25);
        assertThat(response.me().unreadNotiCount()).isEqualTo(1);
    }
}
