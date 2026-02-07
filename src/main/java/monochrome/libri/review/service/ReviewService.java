package monochrome.libri.review.service;

import monochrome.libri.review.dto.request.ReviewCreateRequestDto;
import monochrome.libri.review.dto.request.ReviewUpdateRequestDto;
import monochrome.libri.review.dto.response.ReviewSliceResponseDto;
import monochrome.libri.review.dto.response.ReviewSliceWithStatsResponseDto;
import monochrome.libri.review.dto.response.ReviewStatsResponseDto;
import monochrome.libri.review.dto.response.ReviewSummaryResponseDto;
import monochrome.libri.review.dto.response.MyReviewListResponseDto;
import monochrome.libri.review.dto.response.ReviewBookmarkListResponseDto;
import org.springframework.data.domain.Pageable;

public interface ReviewService {
    void createReview(long memberId, ReviewCreateRequestDto request);
    ReviewSummaryResponseDto updateReview(long reviewId, long memberId, ReviewUpdateRequestDto request);
    void deleteReview(long reviewId, long memberId);
    ReviewSliceWithStatsResponseDto getReviewsByBook(long bookId, Pageable pageable);
    ReviewSliceResponseDto getReviewsByMember(long memberId, Pageable pageable);
    ReviewStatsResponseDto getReviewStats(long bookId);
    long countReviewsByBook(long bookId);
    MyReviewListResponseDto getMyReviews(long memberId, Pageable pageable);
    ReviewBookmarkListResponseDto getBookmarkedReviews(long memberId, Pageable pageable);
}
