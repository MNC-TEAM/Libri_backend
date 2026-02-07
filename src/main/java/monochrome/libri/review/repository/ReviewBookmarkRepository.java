package monochrome.libri.review.repository;

import monochrome.libri.review.domain.ReviewBookmark;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewBookmarkRepository extends JpaRepository<ReviewBookmark, Long> {
    boolean existsByReviewIdAndMemberId(long reviewId, long memberId);
    void deleteByReviewIdAndMemberId(long reviewId, long memberId);
    Optional<ReviewBookmark> findByReviewIdAndMemberId(long reviewId, long memberId);

    @EntityGraph(attributePaths = {"review", "review.book", "review.member"})
    Slice<ReviewBookmark> findByMemberIdOrderByCreatedDateDesc(long memberId, Pageable pageable);

    long countByMemberId(long memberId);
}
