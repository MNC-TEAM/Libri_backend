package monochrome.libri.comment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentCreateRequestDto(
        @NotBlank
        @Size(max = 300)
        String content
) {
}
