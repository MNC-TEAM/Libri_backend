package monochrome.libri.member.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EmailSignUpRequestDto(
        @Email @NotBlank
        String email,
        @NotBlank @Size(min = 8, max = 100)
        String rawPassword,
        @Size(min = 2, max = 30)
        String nickname,
        String profilePath
) {
}
