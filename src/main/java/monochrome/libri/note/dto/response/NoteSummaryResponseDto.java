package monochrome.libri.note.dto.response;

public record NoteSummaryResponseDto(
        Long noteId,
        String content,
        java.time.LocalDateTime createdDate,
        monochrome.libri.note.domain.NoteProgressType progressType,
        int progressValue,
        int likeCount,
        int commentCount,
        boolean isLiked,
        boolean isBookmarked,
        boolean secret
) {
}
