package monochrome.libri.comment.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import monochrome.libri.comment.domain.CommentReportReason;

public record CommentReportCreateRequestDto(
        @NotNull(message = "신고 사유는 필수입니다.")
        CommentReportReason reason,
        @Size(max = 300, message = "상세 설명은 300자 이하여야 합니다.")
        String detail
) {
}
