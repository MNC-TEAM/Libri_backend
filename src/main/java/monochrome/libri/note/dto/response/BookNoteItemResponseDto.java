package monochrome.libri.note.dto.response;

import monochrome.libri.note.domain.NoteProgressType;

import java.time.LocalDateTime;

public record BookNoteItemResponseDto(
        Long noteId,
        Long shelfId,
        Long memberId,
        String nickname,
        NoteProgressType progressType,
        int progressValue,
        LocalDateTime createdDate,
        String content,
        int likeCount,
        int commentCount,
        boolean isLiked,
        boolean isBookmarked
) {
}
