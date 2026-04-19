package monochrome.libri.fcm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * gugumo {@code FcmTokenDto} 와 동일한 JSON 필드명: {@code fcmToken}
 */
public record FcmTokenRequestDto(
        @NotBlank(message = "FCM 토큰은 필수입니다.")
        @Size(max = 512, message = "FCM 토큰은 512자 이하여야 합니다.")
        String fcmToken
) {
}
