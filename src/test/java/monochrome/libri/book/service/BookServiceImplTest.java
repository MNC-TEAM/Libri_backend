package monochrome.libri.book.service;

import monochrome.libri.TestFixtures;
import monochrome.libri.book.domain.Book;
import monochrome.libri.book.dto.request.BookDirectCreateRequestDto;
import monochrome.libri.book.dto.request.BookDirectUpdateRequestDto;
import monochrome.libri.book.repository.BookRepository;
import monochrome.libri.global.exception.LibriException;
import monochrome.libri.member.domain.Member;
import monochrome.libri.member.service.MemberService;
import monochrome.libri.review.domain.Review;
import monochrome.libri.review.domain.ReviewStatus;
import monochrome.libri.review.repository.ReviewRepository;
import monochrome.libri.review.service.ReviewService;
import monochrome.libri.shelf.domain.Shelf;
import monochrome.libri.shelf.domain.ShelfStatus;
import monochrome.libri.shelf.repository.ShelfRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.SliceImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReviewService reviewService;

    @Mock
    private ShelfRepository shelfRepository;

    @Mock
    private MemberService memberService;

    @InjectMocks
    private monochrome.libri.book.service.impl.BookServiceImpl service;

    @Test
    void searchBooks_returnsEmptyForShortKeyword() {
        var slice = service.searchBooks("a", PageRequest.of(0, 10));
        assertThat(slice.getContent()).isEmpty();
    }

    @Test
    void searchBooks_returnsEmptyForInvalidIsbn() {
        var slice = service.searchBooks("9780306406158", PageRequest.of(0, 10));
        assertThat(slice.getContent()).isEmpty();
    }

    @Test
    void searchBooks_returnsBookForValidIsbn() {
        Book book = TestFixtures.book(1L, 100);
        when(bookRepository.findByIsbn("9780306406157")).thenReturn(Optional.of(book));

        var slice = service.searchBooks("978-0-306-40615-7", PageRequest.of(0, 10));

        assertThat(slice.getContent()).hasSize(1);
        assertThat(slice.getContent().get(0).id()).isEqualTo(1L);
    }

    @Test
    void getBookDetail_includesShelfInfoWhenPresent() {
        Book book = TestFixtures.book(1L, 100);
        Member member = TestFixtures.member(1L);
        Shelf shelf = TestFixtures.shelf(2L, member, book);
        Review review = TestFixtures.review(3L, member, book, 5, "great", ReviewStatus.ACTIVE);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(shelfRepository.findByMemberIdAndBookId(1L, 1L)).thenReturn(Optional.of(shelf));
        when(reviewRepository.findByBookIdAndStatusOrderByCreatedDateDesc(eq(1L), eq(ReviewStatus.ACTIVE), any()))
                .thenReturn(new SliceImpl<>(List.of(review), PageRequest.of(0, 3), false));
        when(reviewService.getReviewStats(1L)).thenReturn(null);

        var response = service.getBookDetail(1L, 1L);

        assertThat(response.shelf()).isNotNull();
        assertThat(response.reviews()).hasSize(1);
    }

    @Test
    void createBookDirect_requiresAuth() {
        BookDirectCreateRequestDto dto = new BookDirectCreateRequestDto(
                "title",
                "author",
                "publisher",
                "isbn",
                10,
                ShelfStatus.READING,
                "/cover"
        );

        assertThatThrownBy(() -> service.createBookDirect(0L, dto))
                .isInstanceOf(LibriException.class);
    }

    @Test
    void createBookDirect_savesBookAndShelf() {
        Member member = TestFixtures.member(1L);
        when(memberService.getMemberById(1L)).thenReturn(Optional.of(member));
        when(bookRepository.save(any())).thenAnswer(invocation -> {
            Book book = invocation.getArgument(0);
            return Book.builder()
                    .id(10L)
                    .title(book.getTitle())
                    .author(book.getAuthor())
                    .publisher(book.getPublisher())
                    .isbn(book.getIsbn())
                    .totalPage(book.getTotalPage())
                    .coverImageUrl(book.getCoverImageUrl())
                    .build();
        });
        when(shelfRepository.findByMemberIdAndBookId(1L, 10L)).thenReturn(Optional.empty());

        BookDirectCreateRequestDto dto = new BookDirectCreateRequestDto(
                "title",
                "author",
                "publisher",
                "isbn",
                10,
                ShelfStatus.FINISHED,
                "/cover"
        );

        service.createBookDirect(1L, dto);

        verify(bookRepository).save(any());
        verify(shelfRepository).save(any());
    }

    @Test
    void updateBookDirect_deniesNonOwner() {
        Member owner = TestFixtures.member(1L);
        Member other = TestFixtures.member(2L);
        Book book = Book.builder()
                .id(10L)
                .title("title")
                .author("author")
                .registeredByMember(owner)
                .build();
        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));

        BookDirectUpdateRequestDto dto = new BookDirectUpdateRequestDto(
                "new title",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        assertThatThrownBy(() -> service.updateBookDirect(10L, other.getId(), dto))
                .isInstanceOf(LibriException.class);
    }

    @Test
    void updateBookDirect_updatesOwnedDirectBook() {
        Member owner = TestFixtures.member(1L);
        Book book = Book.builder()
                .id(10L)
                .title("title")
                .author("author")
                .publisher("publisher")
                .isbn("9780306406157")
                .totalPage(10)
                .coverImageUrl("/cover")
                .registeredByMember(owner)
                .build();

        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));
        when(reviewRepository.findByBookIdAndStatusOrderByCreatedDateDesc(eq(10L), eq(ReviewStatus.ACTIVE), any()))
                .thenReturn(new SliceImpl<>(List.of(), PageRequest.of(0, 3), false));
        when(reviewService.getReviewStats(10L)).thenReturn(null);
        when(shelfRepository.findByMemberIdAndBookId(1L, 10L)).thenReturn(Optional.empty());

        BookDirectUpdateRequestDto dto = new BookDirectUpdateRequestDto(
                "  new title  ",
                "  new author ",
                "new publisher",
                "978-0-306-40615-7",
                100,
                "/new-cover",
                " intro ",
                null,
                " /sale "
        );

        var response = service.updateBookDirect(10L, 1L, dto);

        assertThat(book.getTitle()).isEqualTo("new title");
        assertThat(book.getAuthor()).isEqualTo("new author");
        assertThat(book.getPublisher()).isEqualTo("new publisher");
        assertThat(book.getTotalPage()).isEqualTo(100);
        assertThat(book.getCoverImageUrl()).isEqualTo("/new-cover");
        assertThat(book.getIntroduction()).isEqualTo("intro");
        assertThat(book.getSalePageUrl()).isEqualTo("/sale");
        assertThat(response.bookId()).isEqualTo(10L);
    }
}
