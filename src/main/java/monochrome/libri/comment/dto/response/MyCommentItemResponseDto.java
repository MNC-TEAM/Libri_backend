package monochrome.libri.comment.dto.response;

import java.time.LocalDateTime;

public record MyCommentItemResponseDto(
        Long commentId,
        Long noteId,
        Long bookId,
        String bookTitle,
        String bookCoverUrl,
        String noteContent,
        String commentContent,
        LocalDateTime createdDate
) {
}
