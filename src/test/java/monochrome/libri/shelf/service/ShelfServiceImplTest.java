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
import org.mockito.Mockito;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
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
}
