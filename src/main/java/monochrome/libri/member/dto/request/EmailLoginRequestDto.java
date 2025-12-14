package monochrome.libri.member.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailLoginRequestDto(
        @Email
        @NotBlank
        String email,
        @NotBlank
        String password
) {
}
