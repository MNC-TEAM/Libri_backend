package monochrome.libri.shelf.service;

import monochrome.libri.TestFixtures;
import monochrome.libri.book.domain.Book;
import monochrome.libri.book.repository.BookRepository;
import monochrome.libri.global.exception.LibriException;
import monochrome.libri.member.domain.Member;
import monochrome.libri.member.service.MemberService;
import monochrome.libri.review.service.ReviewService;
import monochrome.libri.shelf.service.impl.ShelfServiceImpl;
import monochrome.libri.shelf.domain.Shelf;
import monochrome.libri.shelf.domain.ShelfProgressType;
import monochrome.libri.shelf.domain.ShelfStatus;
import monochrome.libri.shelf.dto.request.ShelfCreateRequestDto;
import monochrome.libri.shelf.dto.request.ShelfUpdateRequestDto;
import monochrome.libri.shelf.repository.ShelfRepository;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ShelfServiceImplTest {

    @Test
    void getShelfDetail_requiresAuth() {
        ShelfRepository shelfRepository = mock(ShelfRepository.class);
        ReviewService reviewService = mock(ReviewService.class);
        MemberService memberService = mock(MemberService.class);
        BookRepository bookRepository = mock(BookRepository.class);
        Clock clock = Clock.fixed(Instant.parse("2024-01-10T00:00:00Z"), ZoneOffset.UTC);
        ShelfServiceImpl service = new ShelfServiceImpl(shelfRepository, reviewService, memberService, bookRepository, clock);

        assertThatThrownBy(() -> service.getShelfDetail(1L, null))
                .isInstanceOf(LibriException.class);
    }

    @Test
    void getShelvesByStatus_returnsEmptyWhenUnauthenticated() {
        ShelfRepository shelfRepository = mock(ShelfRepository.class);
        ReviewService reviewService = mock(ReviewService.class);
        MemberService memberService = mock(MemberService.class);
        BookRepository bookRepository = mock(BookRepository.class);
        Clock clock = Clock.systemUTC();
        ShelfServiceImpl service = new ShelfServiceImpl(shelfRepository, reviewService, memberService, bookRepository, clock);

        var response = service.getShelvesByStatus(null, ShelfStatus.READING, org.springframework.data.domain.PageRequest.of(0, 10));

        assertThat(response.totalCount()).isZero();
        assertThat(response.content()).isEmpty();
        assertThat(response.hasNext()).isFalse();
    }

    @Test
    void getShelvesByStatus_mapsSlice() {
        ShelfRepository shelfRepository = mock(ShelfRepository.class);
        ReviewService reviewService = mock(ReviewService.class);
        MemberService memberService = mock(MemberService.class);
        BookRepository bookRepository = mock(BookRepository.class);
        Clock clock = Clock.systemUTC();
        ShelfServiceImpl service = new ShelfServiceImpl(shelfRepository, reviewService, memberService, bookRepository, clock);

        Member member = TestFixtures.member(1L);
        Book book = TestFixtures.book(2L, 200);
        Shelf shelf = Shelf.builder()
                .id(3L)
                .member(member)
                .book(book)
                .status(ShelfStatus.READING)
                .progressType(ShelfProgressType.PAGE)
                .progressValue(40)
                .currentPage(40)
                .startDate(LocalDate.of(2024, 1, 1))
                .build();

        when(shelfRepository.findByMemberIdAndStatusOrderByCreatedDateDesc(eq(1L), eq(ShelfStatus.READING), any()))
                .thenReturn(new org.springframework.data.domain.SliceImpl<>(
                        java.util.List.of(shelf),
                        org.springframework.data.domain.PageRequest.of(0, 10),
                        false
                ));
        when(shelfRepository.countByMemberIdAndStatus(1L, ShelfStatus.READING)).thenReturn(1L);

        var response = service.getShelvesByStatus(1L, ShelfStatus.READING, org.springframework.data.domain.PageRequest.of(0, 10));

        assertThat(response.totalCount()).isEqualTo(1L);
        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).bookId()).isEqualTo(2L);
        assertThat(response.content().get(0).publisher()).isEqualTo("publisher2");
        assertThat(response.content().get(0).progressPercent()).isEqualTo(20);
    }

    @Test
    void createShelf_noopWhenAlreadyExists() {
        ShelfRepository shelfRepository = mock(ShelfRepository.class);
        ReviewService reviewService = mock(ReviewService.class);
        MemberService memberService = mock(MemberService.class);
        BookRepository bookRepository = mock(BookRepository.class);
        Clock clock = Clock.systemUTC();
        ShelfServiceImpl service = new ShelfServiceImpl(shelfRepository, reviewService, memberService, bookRepository, clock);

        when(shelfRepository.findByMemberIdAndBookId(1L, 2L)).thenReturn(Optional.of(TestFixtures.shelf(1L, TestFixtures.member(1L), TestFixtures.book(2L, 100))));

        ShelfCreateRequestDto dto = new ShelfCreateRequestDto(2L, ShelfStatus.READING, ShelfProgressType.PAGE, 1, null, null);

        service.createShelf(1L, dto);

        verify(shelfRepository, never()).save(any());
    }

    @Test
    void createShelf_validatesProgress() {
        ShelfRepository shelfRepository = mock(ShelfRepository.class);
        ReviewService reviewService = mock(ReviewService.class);
        MemberService memberService = mock(MemberService.class);
        BookRepository bookRepository = mock(BookRepository.class);
        Clock clock = Clock.systemUTC();
        ShelfServiceImpl service = new ShelfServiceImpl(shelfRepository, reviewService, memberService, bookRepository, clock);

        Member member = TestFixtures.member(1L);
        Book book = TestFixtures.book(2L, 10);
        when(shelfRepository.findByMemberIdAndBookId(1L, 2L)).thenReturn(Optional.empty());
        when(memberService.getMemberById(1L)).thenReturn(Optional.of(member));
        when(bookRepository.findById(2L)).thenReturn(Optional.of(book));

        ShelfCreateRequestDto dto = new ShelfCreateRequestDto(2L, ShelfStatus.READING, ShelfProgressType.PAGE, 50, null, null);

        assertThatThrownBy(() -> service.createShelf(1L, dto))
                .isInstanceOf(LibriException.class);
    }

    @Test
    void updateShelf_updatesReadingInfoAndProgress() {
        ShelfRepository shelfRepository = mock(ShelfRepository.class);
        ReviewService reviewService = mock(ReviewService.class);
        MemberService memberService = mock(MemberService.class);
        BookRepository bookRepository = mock(BookRepository.class);
        Clock clock = Clock.fixed(Instant.parse("2024-01-10T00:00:00Z"), ZoneOffset.UTC);
        ShelfServiceImpl service = new ShelfServiceImpl(shelfRepository, reviewService, memberService, bookRepository, clock);

        Member member = TestFixtures.member(1L);
        Book book = TestFixtures.book(2L, 200);
        Shelf shelf = Shelf.builder()
                .id(3L)
                .member(member)
                .book(book)
                .status(ShelfStatus.READING)
                .progressType(ShelfProgressType.PAGE)
                .progressValue(10)
                .currentPage(10)
                .startDate(LocalDate.of(2024, 1, 1))
                .build();

        when(shelfRepository.findByIdAndMemberId(3L, 1L)).thenReturn(Optional.of(shelf));
        when(reviewService.countReviewsByBook(2L)).thenReturn(0L);

        ShelfUpdateRequestDto dto = new ShelfUpdateRequestDto(
                ShelfStatus.FINISHED,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 1, 9),
                ShelfProgressType.PERCENT,
                100
        );

        var response = service.updateShelf(3L, 1L, dto);

        assertThat(response.book().publisher()).isEqualTo("publisher2");
        assertThat(response.reading().status()).isEqualTo(ShelfStatus.FINISHED);
        assertThat(response.reading().progressPercent()).isEqualTo(100);
    }

    @Test
    void getShelvesByStatus_usesProgressValueWhenPageTypeAndCurrentPageIsNull() {
        ShelfRepository shelfRepository = mock(ShelfRepository.class);
        ReviewService reviewService = mock(ReviewService.class);
        MemberService memberService = mock(MemberService.class);
        BookRepository bookRepository = mock(BookRepository.class);
        Clock clock = Clock.systemUTC();
        ShelfServiceImpl service = new ShelfServiceImpl(shelfRepository, reviewService, memberService, bookRepository, clock);

        Member member = TestFixtures.member(1L);
        Book book = TestFixtures.book(2L, 200);
        Shelf shelf = Shelf.builder()
                .id(3L)
                .member(member)
                .book(book)
                .status(ShelfStatus.READING)
                .progressType(ShelfProgressType.PAGE)
                .progressValue(40)
                .currentPage(null)
                .startDate(LocalDate.of(2024, 1, 1))
                .build();

        when(shelfRepository.findByMemberIdAndStatusOrderByCreatedDateDesc(eq(1L), eq(ShelfStatus.READING), any()))
                .thenReturn(new org.springframework.data.domain.SliceImpl<>(
                        java.util.List.of(shelf),
                        org.springframework.data.domain.PageRequest.of(0, 10),
                        false
                ));
        when(shelfRepository.countByMemberIdAndStatus(1L, ShelfStatus.READING)).thenReturn(1L);

        var response = service.getShelvesByStatus(1L, ShelfStatus.READING, org.springframework.data.domain.PageRequest.of(0, 10));

        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).progressPercent()).isEqualTo(20);
    }

    @Test
    void getReadingCalendar_returnsStartedAndFinishedBooksByDay() {
        ShelfRepository shelfRepository = mock(ShelfRepository.class);
        ReviewService reviewService = mock(ReviewService.class);
        MemberService memberService = mock(MemberService.class);
        BookRepository bookRepository = mock(BookRepository.class);
        Clock clock = Clock.fixed(Instant.parse("2024-03-10T00:00:00Z"), ZoneOffset.UTC);
        ShelfServiceImpl service = new ShelfServiceImpl(shelfRepository, reviewService, memberService, bookRepository, clock);

        when(shelfRepository.findCalendarRowsByMemberIdAndDateRange(
                1L,
                LocalDate.of(2024, 3, 1),
                LocalDate.of(2024, 3, 31)
        )).thenReturn(List.of(
                new monochrome.libri.shelf.dto.ShelfCalendarRow(
                        10L,
                        20L,
                        "title",
                        "/cover",
                        LocalDate.of(2024, 3, 5),
                        LocalDate.of(2024, 3, 20),
                        ShelfStatus.FINISHED
                )
        ));

        var response = service.getReadingCalendar(1L, YearMonth.of(2024, 3));

        assertThat(response.days()).hasSize(31);
        assertThat(response.days().get(4).startedBooks()).hasSize(1);
        assertThat(response.days().get(19).finishedBooks()).hasSize(1);
        assertThat(response.days().get(4).startedBooks().get(0).coverUrl()).isEqualTo("/cover");
    }

    @Test
    void getReadingStatistics_returnsYearlyAndMonthlyCounts() {
        ShelfRepository shelfRepository = mock(ShelfRepository.class);
        ReviewService reviewService = mock(ReviewService.class);
        MemberService memberService = mock(MemberService.class);
        BookRepository bookRepository = mock(BookRepository.class);
        Clock clock = Clock.fixed(Instant.parse("2024-03-10T00:00:00Z"), ZoneOffset.UTC);
        ShelfServiceImpl service = new ShelfServiceImpl(shelfRepository, reviewService, memberService, bookRepository, clock);

        when(shelfRepository.countByMemberIdAndStartDateBetween(
                1L,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31)
        )).thenReturn(4L);
        when(shelfRepository.countByMemberIdAndStatusAndEndDateBetween(
                1L,
                ShelfStatus.FINISHED,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31)
        )).thenReturn(3L);
        when(shelfRepository.countFinishedBooksByYear(1L)).thenReturn(List.of(
                new monochrome.libri.shelf.dto.YearCountRow(2023, 2L),
                new monochrome.libri.shelf.dto.YearCountRow(2024, 3L)
        ));
        when(shelfRepository.countFinishedBooksByMonth(1L, 2024)).thenReturn(List.of(
                new monochrome.libri.shelf.dto.MonthCountRow(1, 1L),
                new monochrome.libri.shelf.dto.MonthCountRow(3, 2L)
        ));

        var response = service.getReadingStatistics(1L, 2024);

        assertThat(response.totalStartedCount()).isEqualTo(4L);
        assertThat(response.totalFinishedCount()).isEqualTo(3L);
        assertThat(response.yearlyFinishedCounts()).hasSize(2);
        assertThat(response.monthlyFinishedCounts().get(0).count()).isEqualTo(1L);
        assertThat(response.monthlyCumulativeFinishedCounts().get(2).count()).isEqualTo(3L);
    }
}
