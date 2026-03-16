package monochrome.libri.comment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentCreateRequestDto(
        @NotBlank(message = "댓글 내용은 필수입니다.")
        @Size(max = 300, message = "댓글은 300자 이하여야 합니다.")
        String content
) {
}
