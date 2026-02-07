package monochrome.libri.review.service;

public interface ReviewBookmarkService {
    void bookmark(long reviewId, long memberId);
    void unbookmark(long reviewId, long memberId);
}
