package monochrome.libri.review.service.impl;

import monochrome.libri.book.domain.Book;
import monochrome.libri.book.repository.BookRepository;
import monochrome.libri.global.exception.ErrorCode;
import monochrome.libri.global.exception.LibriException;
import monochrome.libri.member.domain.Member;
import monochrome.libri.member.service.MemberService;
import monochrome.libri.review.domain.Review;
import monochrome.libri.review.domain.ReviewStatus;
import monochrome.libri.review.dto.request.ReviewCreateRequestDto;
import monochrome.libri.review.dto.request.ReviewUpdateRequestDto;
import monochrome.libri.review.dto.response.ReviewBookmarkItemResponseDto;
import monochrome.libri.review.dto.response.ReviewBookmarkListResponseDto;
import monochrome.libri.review.dto.response.MyReviewItemResponseDto;
import monochrome.libri.review.dto.response.MyReviewListResponseDto;
import monochrome.libri.review.dto.response.ReviewSliceResponseDto;
import monochrome.libri.review.dto.response.ReviewSliceWithStatsResponseDto;
import monochrome.libri.review.dto.response.ReviewStatsResponseDto;
import monochrome.libri.review.dto.response.ReviewSummaryResponseDto;
import monochrome.libri.review.repository.RatingCountRow;
import monochrome.libri.review.repository.ReviewBookmarkRepository;
import monochrome.libri.review.repository.ReviewRepository;
import monochrome.libri.review.service.ReviewService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewBookmarkRepository reviewBookmarkRepository;
    private final MemberService memberService;
    private final BookRepository bookRepository;

    public ReviewServiceImpl(
            ReviewRepository reviewRepository,
            ReviewBookmarkRepository reviewBookmarkRepository,
            MemberService memberService,
            BookRepository bookRepository
    ) {
        this.reviewRepository = reviewRepository;
        this.reviewBookmarkRepository = reviewBookmarkRepository;
        this.memberService = memberService;
        this.bookRepository = bookRepository;
    }

    @Override
    @Transactional
    public void createReview(long memberId, ReviewCreateRequestDto request) {
        if (memberId <= 0) {
            throw new LibriException(ErrorCode.AUTHENTICATION_FAILED);
        }
        if (request == null) {
            throw new LibriException(ErrorCode.INVALID_INPUT_VALUE);
        }

        Member member = memberService.getMemberById(memberId)
                .orElseThrow(() -> new LibriException(ErrorCode.MEMBER_NOT_FOUND));

        Book book = bookRepository.findById(request.bookId())
                .orElseThrow(() -> new LibriException(ErrorCode.BOOK_NOT_FOUND));

        Review review = Review.builder()
                .book(book)
                .member(member)
                .rating(request.rating())
                .content(request.content().trim())
                .status(ReviewStatus.ACTIVE)
                .build();

        reviewRepository.save(review);
    }

    @Override
    @Transactional
    public ReviewSummaryResponseDto updateReview(long reviewId, long memberId, ReviewUpdateRequestDto request) {
        if (memberId <= 0) {
            throw new LibriException(ErrorCode.AUTHENTICATION_FAILED);
        }
        if (request == null || (request.rating() == null && (request.content() == null || request.content().isBlank()))) {
            throw new LibriException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (request.content() != null && request.content().isBlank()) {
            throw new LibriException(ErrorCode.INVALID_INPUT_VALUE);
        }

        Review review = reviewRepository.findByIdAndStatus(reviewId, ReviewStatus.ACTIVE)
                .orElseThrow(() -> new LibriException(ErrorCode.REVIEW_NOT_FOUND));

        if (review.getMember().getId() != memberId) {
            throw new LibriException(ErrorCode.ACCESS_DENIED);
        }

        review.update(
                request.rating(),
                request.content() == null ? null : request.content().trim()
        );

        return toSummary(review);
    }

    @Override
    @Transactional
    public void deleteReview(long reviewId, long memberId) {
        if (memberId <= 0) {
            throw new LibriException(ErrorCode.AUTHENTICATION_FAILED);
        }

        Review review = reviewRepository.findByIdAndStatus(reviewId, ReviewStatus.ACTIVE)
                .orElseThrow(() -> new LibriException(ErrorCode.REVIEW_NOT_FOUND));

        if (review.getMember().getId() != memberId) {
            throw new LibriException(ErrorCode.ACCESS_DENIED);
        }

        review.delete();
    }

    @Override
    public ReviewSliceWithStatsResponseDto getReviewsByBook(long bookId, Pageable pageable) {
        ensureBookExists(bookId);

        Slice<Review> slice = reviewRepository.findByBookIdAndStatusOrderByCreatedDateDesc(
                bookId,
                ReviewStatus.ACTIVE,
                pageable
        );

        List<ReviewSummaryResponseDto> content = slice.getContent().stream()
                .map(this::toSummary)
                .toList();

        ReviewStatsResponseDto stats = getReviewStats(bookId);

        return new ReviewSliceWithStatsResponseDto(
                stats,
                content,
                slice.hasNext(),
                slice.getNumber(),
                slice.getSize()
        );
    }

    @Override
    public ReviewSliceResponseDto getReviewsByMember(long memberId, Pageable pageable) {
        if (memberId <= 0) {
            throw new LibriException(ErrorCode.AUTHENTICATION_FAILED);
        }

        Slice<Review> slice = reviewRepository.findByMemberIdAndStatusOrderByCreatedDateDesc(
                memberId,
                ReviewStatus.ACTIVE,
                pageable
        );

        List<ReviewSummaryResponseDto> content = slice.getContent().stream()
                .map(this::toSummary)
                .toList();

        return new ReviewSliceResponseDto(
                content,
                slice.hasNext(),
                slice.getNumber(),
                slice.getSize()
        );
    }

    @Override
    public ReviewStatsResponseDto getReviewStats(long bookId) {
        ensureBookExists(bookId);

        long reviewCount = reviewRepository.countByBookIdAndStatus(bookId, ReviewStatus.ACTIVE);
        double avgRating = reviewRepository.getAverageRatingByBookIdAndStatus(bookId, ReviewStatus.ACTIVE);
        Map<Integer, Long> ratingCounts = buildRatingCounts(
                reviewRepository.getRatingCountsByBookIdAndStatus(bookId, ReviewStatus.ACTIVE)
        );

        return new ReviewStatsResponseDto(
                avgRating,
                reviewCount,
                ratingCounts.getOrDefault(1, 0L),
                ratingCounts.getOrDefault(2, 0L),
                ratingCounts.getOrDefault(3, 0L),
                ratingCounts.getOrDefault(4, 0L),
                ratingCounts.getOrDefault(5, 0L)
        );
    }

    @Override
    public long countReviewsByBook(long bookId) {
        ensureBookExists(bookId);
        return reviewRepository.countByBookIdAndStatus(bookId, ReviewStatus.ACTIVE);
    }

    @Override
    public MyReviewListResponseDto getMyReviews(long memberId, Pageable pageable) {
        if (memberId <= 0) {
            throw new LibriException(ErrorCode.AUTHENTICATION_FAILED);
        }
        Slice<Review> slice = reviewRepository.findByMemberIdAndStatusOrderByCreatedDateDesc(
                memberId,
                ReviewStatus.ACTIVE,
                pageable
        );

        List<MyReviewItemResponseDto> content = slice.getContent().stream()
                .map(review -> new MyReviewItemResponseDto(
                        review.getId(),
                        review.getBook().getId(),
                        review.getBook().getTitle(),
                        review.getRating(),
                        review.getContent()
                ))
                .toList();

        long totalCount = reviewRepository.countByMemberIdAndStatus(memberId, ReviewStatus.ACTIVE);

        return new MyReviewListResponseDto(
                totalCount,
                content,
                slice.hasNext(),
                slice.getNumber(),
                slice.getSize()
        );
    }

    @Override
    public ReviewBookmarkListResponseDto getBookmarkedReviews(long memberId, Pageable pageable) {
        if (memberId <= 0) {
            throw new LibriException(ErrorCode.AUTHENTICATION_FAILED);
        }

        var slice = reviewBookmarkRepository.findByMemberIdOrderByCreatedDateDesc(memberId, pageable);
        List<ReviewBookmarkItemResponseDto> content = slice.getContent().stream()
                .map(bookmark -> new ReviewBookmarkItemResponseDto(
                        bookmark.getReview().getId(),
                        bookmark.getReview().getBook().getId(),
                        bookmark.getReview().getBook().getTitle(),
                        bookmark.getReview().getMember().getNickname(),
                        bookmark.getReview().getRating(),
                        bookmark.getReview().getContent()
                ))
                .toList();

        long totalCount = reviewBookmarkRepository.countByMemberId(memberId);

        return new ReviewBookmarkListResponseDto(
                totalCount,
                content,
                slice.hasNext(),
                slice.getNumber(),
                slice.getSize()
        );
    }

    private void ensureBookExists(long bookId) {
        if (!bookRepository.existsById(bookId)) {
            throw new LibriException(ErrorCode.BOOK_NOT_FOUND);
        }
    }

    private ReviewSummaryResponseDto toSummary(Review review) {
        return new ReviewSummaryResponseDto(
                review.getId(),
                review.getMember().getId(),
                review.getMember().getNickname(),
                review.getRating(),
                review.getContent()
        );
    }

    private Map<Integer, Long> buildRatingCounts(List<RatingCountRow> rows) {
        Map<Integer, Long> counts = new LinkedHashMap<>();
        for (int rating = 1; rating <= 5; rating++) {
            counts.put(rating, 0L);
        }
        for (RatingCountRow row : rows) {
            if (row.getRating() != null) {
                counts.put(row.getRating(), row.getCount() == null ? 0L : row.getCount());
            }
        }
        return counts;
    }
}
