package monochrome.libri.note.repository;

import monochrome.libri.note.domain.NoteLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface NoteLikeRepository extends JpaRepository<NoteLike, Long> {
    boolean existsByNoteIdAndMemberId(long noteId, long memberId);
    Optional<NoteLike> findByNoteIdAndMemberId(long noteId, long memberId);
    void deleteByNoteIdAndMemberId(long noteId, long memberId);
    void deleteByNoteId(long noteId);
    @Query("select nl.note.id from NoteLike nl where nl.member.id = :memberId and nl.note.id in :noteIds")
    List<Long> findNoteIdsByMemberIdAndNoteIdIn(@Param("memberId") long memberId, @Param("noteIds") Collection<Long> noteIds);
    long countByNoteId(long noteId);
}
