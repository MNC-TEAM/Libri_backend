package monochrome.libri.comment.service;

import monochrome.libri.comment.dto.request.CommentCreateRequestDto;
import monochrome.libri.comment.dto.request.CommentReportCreateRequestDto;
import monochrome.libri.comment.dto.response.CommentSliceResponseDto;
import monochrome.libri.comment.dto.response.MyCommentListResponseDto;
import org.springframework.data.domain.Pageable;

public interface NoteCommentService {
    void createComment(long noteId, long memberId, CommentCreateRequestDto request);
    void reportComment(long noteId, long commentId, long memberId, CommentReportCreateRequestDto request);
    CommentSliceResponseDto getComments(long noteId, long memberId, Pageable pageable);
    void deleteComment(long noteId, long commentId, long memberId);
    MyCommentListResponseDto getCommentsByMember(long memberId, Pageable pageable);
}
