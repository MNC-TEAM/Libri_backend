package monochrome.libri.comment.repository;

import monochrome.libri.comment.domain.NoteComment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.List;

@Repository
public interface NoteCommentRepository extends JpaRepository<NoteComment, Long> {
    @EntityGraph(attributePaths = {"member"})
    Slice<NoteComment> findByNoteIdOrderByCreatedDateDesc(long noteId, Pageable pageable);

    long countByNoteId(long noteId);

    void deleteByNoteId(long noteId);

    Optional<NoteComment> findByIdAndNoteId(long commentId, long noteId);

    @EntityGraph(attributePaths = {"note", "note.member", "member"})
    Optional<NoteComment> findWithNoteAndMemberByIdAndNoteId(long commentId, long noteId);

    @Query("select nc.note.id as noteId, count(nc) as count " +
            "from NoteComment nc where nc.note.id in :noteIds group by nc.note.id")
    List<NoteCommentCountRow> countByNoteIds(@Param("noteIds") Collection<Long> noteIds);

    @EntityGraph(attributePaths = {"note", "note.shelf", "note.shelf.book"})
    Slice<NoteComment> findByMemberIdOrderByCreatedDateDesc(long memberId, Pageable pageable);

    long countByMemberId(long memberId);
}
