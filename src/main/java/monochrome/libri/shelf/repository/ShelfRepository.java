package monochrome.libri.shelf.repository;

import monochrome.libri.shelf.domain.Shelf;
import monochrome.libri.shelf.domain.ShelfStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ShelfRepository extends JpaRepository<Shelf, Long>, ShelfRepositoryCustom {
    Optional<Shelf> findByIdAndMemberId(long id, long memberId);
    Optional<Shelf> findByMemberIdAndBookId(long memberId, long bookId);

    @EntityGraph(attributePaths = "book")
    Slice<Shelf> findByMemberIdAndStatusOrderByCreatedDateDesc(long memberId, ShelfStatus status, Pageable pageable);

    long countByMemberIdAndStatus(long memberId, ShelfStatus status);
    long countByMemberIdAndStartDateBetween(long memberId, LocalDate startDate, LocalDate endDate);
    long countByMemberIdAndStatusAndEndDateBetween(long memberId, ShelfStatus status, LocalDate startDate, LocalDate endDate);
}
