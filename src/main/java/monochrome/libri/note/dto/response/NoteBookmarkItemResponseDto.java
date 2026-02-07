package monochrome.libri.note.dto.response;

import java.time.LocalDateTime;

public record NoteBookmarkItemResponseDto(
        Long noteId,
        Long bookId,
        String bookTitle,
        String bookCoverUrl,
        String authorNickname,
        monochrome.libri.note.domain.NoteProgressType progressType,
        int progressValue,
        LocalDateTime createdDate,
        String content,
        boolean secret,
        int likeCount,
        int commentCount,
        boolean isLiked,
        boolean isBookmarked
) {
}
