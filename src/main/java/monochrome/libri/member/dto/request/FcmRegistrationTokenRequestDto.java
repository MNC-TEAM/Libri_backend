package monochrome.libri.member.dto.request;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Size;

public record FcmRegistrationTokenRequestDto(
        @Nullable
        @Size(max = 512, message = "FCM 토큰은 512자 이하여야 합니다.")
        String token
) {
}
