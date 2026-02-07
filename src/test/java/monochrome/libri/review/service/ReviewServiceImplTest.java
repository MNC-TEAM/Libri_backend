package monochrome.libri.review.service;

import monochrome.libri.TestFixtures;
import monochrome.libri.book.domain.Book;
import monochrome.libri.book.repository.BookRepository;
import monochrome.libri.global.exception.LibriException;
import monochrome.libri.member.domain.Member;
import monochrome.libri.member.service.MemberService;
import monochrome.libri.review.domain.Review;
import monochrome.libri.review.domain.ReviewStatus;
import monochrome.libri.review.dto.request.ReviewCreateRequestDto;
import monochrome.libri.review.dto.request.ReviewUpdateRequestDto;
import monochrome.libri.review.repository.RatingCountRow;
import monochrome.libri.review.repository.ReviewBookmarkRepository;
import monochrome.libri.review.repository.ReviewRepository;
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
class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReviewBookmarkRepository reviewBookmarkRepository;

    @Mock
    private MemberService memberService;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private monochrome.libri.review.service.impl.ReviewServiceImpl service;

    @Test
    void createReview_requiresAuth() {
        ReviewCreateRequestDto dto = new ReviewCreateRequestDto(1L, 5, "content");
        assertThatThrownBy(() -> service.createReview(0L, dto))
                .isInstanceOf(LibriException.class);
    }

    @Test
    void createReview_savesTrimmedContent() {
        Member member = TestFixtures.member(1L);
        Book book = TestFixtures.book(2L, 100);

        when(memberService.getMemberById(1L)).thenReturn(Optional.of(member));
        when(bookRepository.findById(2L)).thenReturn(Optional.of(book));

        ReviewCreateRequestDto dto = new ReviewCreateRequestDto(2L, 4, "  nice ");
        service.createReview(1L, dto);

        ArgumentCaptor<Review> captor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).save(captor.capture());
        assertThat(captor.getValue().getContent()).isEqualTo("nice");
        assertThat(captor.getValue().getStatus()).isEqualTo(ReviewStatus.ACTIVE);
    }

    @Test
    void updateReview_rejectsInvalidRequest() {
        assertThatThrownBy(() -> service.updateReview(1L, 1L, new ReviewUpdateRequestDto(null, " ")))
                .isInstanceOf(LibriException.class);
    }

    @Test
    void updateReview_requiresOwner() {
        Member member = TestFixtures.member(1L);
        Review review = TestFixtures.review(1L, member, TestFixtures.book(1L, 100), 5, "ok", ReviewStatus.ACTIVE);
        when(reviewRepository.findByIdAndStatus(1L, ReviewStatus.ACTIVE)).thenReturn(Optional.of(review));

        assertThatThrownBy(() -> service.updateReview(1L, 999L, new ReviewUpdateRequestDto(4, "fine")))
                .isInstanceOf(LibriException.class);
    }

    @Test
    void deleteReview_requiresOwner() {
        Member member = TestFixtures.member(1L);
        Review review = TestFixtures.review(1L, member, TestFixtures.book(1L, 100), 5, "ok", ReviewStatus.ACTIVE);
        when(reviewRepository.findByIdAndStatus(1L, ReviewStatus.ACTIVE)).thenReturn(Optional.of(review));

        assertThatThrownBy(() -> service.deleteReview(1L, 999L))
                .isInstanceOf(LibriException.class);
    }

    @Test
    void getReviewStats_mapsRatingCounts() {
        when(bookRepository.existsById(1L)).thenReturn(true);
        when(reviewRepository.countByBookIdAndStatus(1L, ReviewStatus.ACTIVE)).thenReturn(2L);
        when(reviewRepository.getAverageRatingByBookIdAndStatus(1L, ReviewStatus.ACTIVE)).thenReturn(4.5);

        RatingCountRow row = new RatingCountRow() {
            @Override
            public Integer getRating() {
                return 5;
            }

            @Override
            public Long getCount() {
                return 2L;
            }
        };

        when(reviewRepository.getRatingCountsByBookIdAndStatus(1L, ReviewStatus.ACTIVE)).thenReturn(List.of(row));

        var stats = service.getReviewStats(1L);

        assertThat(stats.reviewCount()).isEqualTo(2L);
        assertThat(stats.rating5()).isEqualTo(2L);
        assertThat(stats.rating1()).isEqualTo(0L);
    }

    @Test
    void getReviewsByBook_requiresBook() {
        when(bookRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> service.getReviewsByBook(1L, PageRequest.of(0, 10)))
                .isInstanceOf(LibriException.class);
    }

    @Test
    void getReviewsByMember_mapsSlice() {
        Member member = TestFixtures.member(1L);
        Review review = TestFixtures.review(1L, member, TestFixtures.book(1L, 100), 4, "good", ReviewStatus.ACTIVE);

        when(reviewRepository.findByMemberIdAndStatusOrderByCreatedDateDesc(eq(1L), eq(ReviewStatus.ACTIVE), any()))
                .thenReturn(new SliceImpl<>(List.of(review), PageRequest.of(0, 10), false));

        var response = service.getReviewsByMember(1L, PageRequest.of(0, 10));

        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).rating()).isEqualTo(4);
    }
}
