package monochrome.libri.home.service;

import monochrome.libri.TestFixtures;
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
        ShelfRepository shelfRepository = mock(ShelfRepository.class);
        Clock clock = Clock.fixed(Instant.parse("2024-01-10T00:00:00Z"), ZoneOffset.UTC);

        HomeServiceImpl service = new HomeServiceImpl(memberService, shelfRepository, clock);

        HomeResponseDto response = service.getHome(null);

        assertThat(response.me().memberId()).isNull();
        assertThat(response.shelfSummary().wantToReadCount()).isEqualTo(0);
        assertThat(response.readingBooks()).isEmpty();
    }

    @Test
    void getHome_buildsReadingBooksAndCounts() {
        MemberService memberService = mock(MemberService.class);
        ShelfRepository shelfRepository = mock(ShelfRepository.class);
        Clock clock = Clock.fixed(Instant.parse("2024-01-10T00:00:00Z"), ZoneOffset.UTC);

        Member member = TestFixtures.member(1L);
        when(memberService.getMemberById(1L)).thenReturn(Optional.of(member));
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

        HomeServiceImpl service = new HomeServiceImpl(memberService, shelfRepository, clock);
        HomeResponseDto response = service.getHome(1L);

        assertThat(response.shelfSummary().wantToReadCount()).isEqualTo(1);
        assertThat(response.readingBooks()).hasSize(1);
        HomeResponseDto.ReadingBook book = response.readingBooks().get(0);
        assertThat(book.progressPercent()).isEqualTo(25);
        assertThat(book.readingDays()).isEqualTo(3);
    }
}
