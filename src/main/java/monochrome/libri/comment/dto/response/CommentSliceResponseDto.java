package monochrome.libri.comment.dto.response;

import java.util.List;

public record CommentSliceResponseDto(
        long totalCount,
        List<CommentResponseDto> content,
        boolean hasNext,
        int page,
        int size
) {
}
