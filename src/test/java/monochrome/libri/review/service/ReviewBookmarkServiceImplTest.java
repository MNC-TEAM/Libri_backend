package monochrome.libri.review.service;

import monochrome.libri.TestFixtures;
import monochrome.libri.global.exception.LibriException;
import monochrome.libri.member.domain.Member;
import monochrome.libri.member.service.MemberService;
import monochrome.libri.review.domain.Review;
import monochrome.libri.review.domain.ReviewStatus;
import monochrome.libri.review.repository.ReviewBookmarkRepository;
import monochrome.libri.review.repository.ReviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewBookmarkServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReviewBookmarkRepository reviewBookmarkRepository;

    @Mock
    private MemberService memberService;

    @InjectMocks
    private monochrome.libri.review.service.impl.ReviewBookmarkServiceImpl service;

    @Test
    void bookmark_requiresAuth() {
        assertThatThrownBy(() -> service.bookmark(1L, 0L))
                .isInstanceOf(LibriException.class);
    }

    @Test
    void bookmark_noopWhenAlreadyBookmarked() {
        Review review = TestFixtures.review(1L, TestFixtures.member(1L), TestFixtures.book(1L, 100), 5, "ok", ReviewStatus.ACTIVE);
        when(reviewRepository.findWithBookByIdAndStatus(1L, ReviewStatus.ACTIVE)).thenReturn(Optional.of(review));
        when(reviewBookmarkRepository.existsByReviewIdAndMemberId(1L, 2L)).thenReturn(true);

        service.bookmark(1L, 2L);

        verify(reviewBookmarkRepository, never()).save(any());
    }

    @Test
    void bookmark_savesWhenNew() {
        Member member = TestFixtures.member(2L);
        Review review = TestFixtures.review(1L, TestFixtures.member(1L), TestFixtures.book(1L, 100), 5, "ok", ReviewStatus.ACTIVE);
        when(reviewRepository.findWithBookByIdAndStatus(1L, ReviewStatus.ACTIVE)).thenReturn(Optional.of(review));
        when(reviewBookmarkRepository.existsByReviewIdAndMemberId(1L, 2L)).thenReturn(false);
        when(memberService.getMemberById(2L)).thenReturn(Optional.of(member));

        service.bookmark(1L, 2L);

        verify(reviewBookmarkRepository).save(any());
    }

    @Test
    void unbookmark_noopWhenNotBookmarked() {
        Review review = TestFixtures.review(1L, TestFixtures.member(1L), TestFixtures.book(1L, 100), 5, "ok", ReviewStatus.ACTIVE);
        when(reviewRepository.findWithBookByIdAndStatus(1L, ReviewStatus.ACTIVE)).thenReturn(Optional.of(review));
        when(reviewBookmarkRepository.existsByReviewIdAndMemberId(1L, 2L)).thenReturn(false);

        service.unbookmark(1L, 2L);

        verify(reviewBookmarkRepository, never()).deleteByReviewIdAndMemberId(anyLong(), anyLong());
    }

    @Test
    void unbookmark_deletesWhenExists() {
        Review review = TestFixtures.review(1L, TestFixtures.member(1L), TestFixtures.book(1L, 100), 5, "ok", ReviewStatus.ACTIVE);
        when(reviewRepository.findWithBookByIdAndStatus(1L, ReviewStatus.ACTIVE)).thenReturn(Optional.of(review));
        when(reviewBookmarkRepository.existsByReviewIdAndMemberId(1L, 2L)).thenReturn(true);

        service.unbookmark(1L, 2L);

        verify(reviewBookmarkRepository).deleteByReviewIdAndMemberId(1L, 2L);
    }
}
