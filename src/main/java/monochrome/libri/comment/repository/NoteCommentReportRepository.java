package monochrome.libri.comment.repository;

import monochrome.libri.comment.domain.NoteComment;
import monochrome.libri.comment.domain.NoteCommentReport;
import monochrome.libri.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NoteCommentReportRepository extends JpaRepository<NoteCommentReport, Long> {
    boolean existsByNoteCommentAndReporter(NoteComment noteComment, Member reporter);

    @Modifying
    @Query("delete from NoteCommentReport ncr where ncr.noteComment.note.id = :noteId")
    void deleteByNoteId(@Param("noteId") long noteId);
}
