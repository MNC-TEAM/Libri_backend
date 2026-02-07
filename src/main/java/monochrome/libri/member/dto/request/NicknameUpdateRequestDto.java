package monochrome.libri.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NicknameUpdateRequestDto(
        @NotBlank
        @Size(min = 1, max = 30)
        String nickname
) {
}
