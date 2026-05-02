package monochrome.libri.comment.repository;

import monochrome.libri.comment.domain.NoteComment;
import monochrome.libri.comment.domain.NoteCommentReport;
import monochrome.libri.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoteCommentReportRepository extends JpaRepository<NoteCommentReport, Long> {
    boolean existsByNoteCommentAndReporter(NoteComment noteComment, Member reporter);
}
