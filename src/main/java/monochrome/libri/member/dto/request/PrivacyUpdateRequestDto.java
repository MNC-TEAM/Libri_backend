package monochrome.libri.member.dto.request;

import jakarta.validation.constraints.NotNull;

public record PrivacyUpdateRequestDto(
        @NotNull(message = "비공개 여부는 필수입니다.") Boolean privateAccount
) {
}
