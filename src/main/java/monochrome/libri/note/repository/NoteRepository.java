package monochrome.libri.note.repository;

import monochrome.libri.note.domain.Note;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByShelfIdOrderByCreatedDateDesc(long shelfId);
    List<Note> findByShelfIdAndSecretFalseOrderByCreatedDateDesc(long shelfId);
    Slice<Note> findByShelfIdOrderByCreatedDateDesc(long shelfId, Pageable pageable);
    Slice<Note> findByShelfIdAndSecretFalseOrderByCreatedDateDesc(long shelfId, Pageable pageable);
    @EntityGraph(attributePaths = {"shelf", "shelf.book"})
    Slice<Note> findByMemberId(long memberId, Pageable pageable);
    long countByMemberId(long memberId);
    @EntityGraph(attributePaths = {"shelf", "shelf.book", "member"})
    Optional<Note> findWithBookById(long noteId);
    @EntityGraph(attributePaths = {"member"})
    Optional<Note> findWithMemberById(long noteId);
    int countByShelfId(long shelfId);
    Optional<Note> findByIdAndShelfId(long noteId, long shelfId);
}
