package monochrome.libri.comment.dto.response;

import java.time.LocalDateTime;

public record CommentResponseDto(
        Long commentId,
        Long memberId,
        String nickname,
        String content,
        LocalDateTime createdDate,
        boolean isOwner
) {
}
