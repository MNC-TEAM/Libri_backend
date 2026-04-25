package monochrome.libri.member.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ProfileImageUpdateRequestDto(
        @NotBlank(message = "프로필 이미지 경로는 필수입니다.")
        String profilePath
) {
}
