package monochrome.libri.review.service.impl;

import monochrome.libri.global.exception.ErrorCode;
import monochrome.libri.global.exception.LibriException;
import monochrome.libri.member.domain.Member;
import monochrome.libri.member.service.MemberService;
import monochrome.libri.review.domain.Review;
import monochrome.libri.review.domain.ReviewBookmark;
import monochrome.libri.review.domain.ReviewStatus;
import monochrome.libri.review.repository.ReviewBookmarkRepository;
import monochrome.libri.review.repository.ReviewRepository;
import monochrome.libri.review.service.ReviewBookmarkService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReviewBookmarkServiceImpl implements ReviewBookmarkService {

    private final ReviewRepository reviewRepository;
    private final ReviewBookmarkRepository reviewBookmarkRepository;
    private final MemberService memberService;

    public ReviewBookmarkServiceImpl(
            ReviewRepository reviewRepository,
            ReviewBookmarkRepository reviewBookmarkRepository,
            MemberService memberService
    ) {
        this.reviewRepository = reviewRepository;
        this.reviewBookmarkRepository = reviewBookmarkRepository;
        this.memberService = memberService;
    }

    @Override
    @Transactional
    public void bookmark(long reviewId, long memberId) {
        if (memberId <= 0) {
            throw new LibriException(ErrorCode.AUTHENTICATION_FAILED);
        }

        Review review = reviewRepository.findWithBookByIdAndStatus(reviewId, ReviewStatus.ACTIVE)
                .orElseThrow(() -> new LibriException(ErrorCode.REVIEW_NOT_FOUND));

        if (reviewBookmarkRepository.existsByReviewIdAndMemberId(reviewId, memberId)) {
            return;
        }

        Member member = memberService.getMemberById(memberId)
                .orElseThrow(() -> new LibriException(ErrorCode.MEMBER_NOT_FOUND));

        ReviewBookmark bookmark = ReviewBookmark.builder()
                .review(review)
                .member(member)
                .build();

        reviewBookmarkRepository.save(bookmark);
    }

    @Override
    @Transactional
    public void unbookmark(long reviewId, long memberId) {
        if (memberId <= 0) {
            throw new LibriException(ErrorCode.AUTHENTICATION_FAILED);
        }

        reviewRepository.findWithBookByIdAndStatus(reviewId, ReviewStatus.ACTIVE)
                .orElseThrow(() -> new LibriException(ErrorCode.REVIEW_NOT_FOUND));

        if (!reviewBookmarkRepository.existsByReviewIdAndMemberId(reviewId, memberId)) {
            return;
        }

        reviewBookmarkRepository.deleteByReviewIdAndMemberId(reviewId, memberId);
    }
}
