package monochrome.libri.shelf.repository;

import monochrome.libri.shelf.domain.Shelf;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShelfRepository extends JpaRepository<Shelf, Long>, ShelfRepositoryCustom {
    Optional<Shelf> findByIdAndMemberId(long id, long memberId);
    Optional<Shelf> findByMemberIdAndBookId(long memberId, long bookId);
}
