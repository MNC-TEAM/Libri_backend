package monochrome.libri.review.repository;

import monochrome.libri.review.domain.Review;
import monochrome.libri.review.domain.ReviewStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    @EntityGraph(attributePaths = {"member"})
    Slice<Review> findByBookIdAndStatusOrderByCreatedDateDesc(long bookId, ReviewStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"book"})
    Slice<Review> findByMemberIdAndStatusOrderByCreatedDateDesc(long memberId, ReviewStatus status, Pageable pageable);

    Optional<Review> findByIdAndStatus(long reviewId, ReviewStatus status);

    @EntityGraph(attributePaths = {"book", "member"})
    Optional<Review> findWithBookByIdAndStatus(long reviewId, ReviewStatus status);

    long countByBookIdAndStatus(long bookId, ReviewStatus status);
    long countByMemberIdAndStatus(long memberId, ReviewStatus status);

    @Query("select coalesce(avg(r.rating), 0) from Review r where r.book.id = :bookId and r.status = :status")
    double getAverageRatingByBookIdAndStatus(@Param("bookId") long bookId, @Param("status") ReviewStatus status);

    @Query("select r.rating as rating, count(r) as count " +
            "from Review r " +
            "where r.book.id = :bookId and r.status = :status " +
            "group by r.rating")
    List<RatingCountRow> getRatingCountsByBookIdAndStatus(@Param("bookId") long bookId, @Param("status") ReviewStatus status);
}
