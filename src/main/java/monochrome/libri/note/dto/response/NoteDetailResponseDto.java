package monochrome.libri.note.dto.response;

import java.time.LocalDateTime;

public record NoteDetailResponseDto(
        Long noteId,
        Long shelfId,
        String content,
        LocalDateTime createdDate,
        monochrome.libri.note.domain.NoteProgressType progressType,
        int progressValue,
        boolean secret,
        int likeCount,
        int commentCount,
        boolean isLiked,
        boolean isBookmarked,
        boolean isOwner,
        BookInfo book
) {
    public record BookInfo(
            Long bookId,
            String title,
            String author,
            String coverUrl
    ) {
    }
}
