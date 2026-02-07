package monochrome.libri.note.repository;

import monochrome.libri.note.domain.NoteBookmark;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface NoteBookmarkRepository extends JpaRepository<NoteBookmark, Long> {
    boolean existsByNoteIdAndMemberId(long noteId, long memberId);
    Optional<NoteBookmark> findByNoteIdAndMemberId(long noteId, long memberId);
    void deleteByNoteIdAndMemberId(long noteId, long memberId);
    @Query("select nb.note.id from NoteBookmark nb where nb.member.id = :memberId and nb.note.id in :noteIds")
    List<Long> findNoteIdsByMemberIdAndNoteIdIn(@Param("memberId") long memberId, @Param("noteIds") Collection<Long> noteIds);
    long countByNoteId(long noteId);
    @EntityGraph(attributePaths = {"note", "note.shelf", "note.shelf.book", "note.member"})
    Slice<NoteBookmark> findByMemberIdOrderByCreatedDateDesc(long memberId, Pageable pageable);
    long countByMemberId(long memberId);
}
