package monochrome.libri.member.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import monochrome.libri.member.domain.MemberReportReason;

public record MemberReportCreateRequestDto(
        @NotNull(message = "신고 사유는 필수입니다.")
        MemberReportReason reason,
        @Size(max = 300, message = "상세 설명은 300자 이하여야 합니다.")
        String detail
) {
}
