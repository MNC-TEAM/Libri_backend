package monochrome.libri.comment.dto.response;

import java.util.List;

public record MyCommentListResponseDto(
        long totalCount,
        List<MyCommentItemResponseDto> content,
        boolean hasNext,
        int page,
        int size
) {
}
