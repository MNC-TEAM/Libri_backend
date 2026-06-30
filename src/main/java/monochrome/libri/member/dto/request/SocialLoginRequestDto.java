package monochrome.libri.member.dto.request;

import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;
import monochrome.libri.member.domain.SignType;

public record SocialLoginRequestDto(
        @Schema(
                description = "소셜 로그인 제공자. KAKAO, APPLE, GOOGLE 지원",
                example = "KAKAO"
        )
        @NotNull(message = "소셜 로그인 제공자는 필수입니다.")
        SignType provider,
        @Schema(
                description = "소셜 로그인 후 프론트가 받은 id token(JWT). provider=APPLE 또는 provider=GOOGLE 일 때 사용",
                example = "eyJraWQiOiJ...id-token"
        )
        String idToken,
        @Schema(
                description = "카카오 로그인 후 프론트가 받은 access token. provider=KAKAO 일 때 사용",
                example = "eyJhbGciOiJIUzI1NiJ9...kakao-access-token"
        )
        String accessToken,
        @Schema(
                description = "카카오 로그인 후 프론트가 받은 authorization code. provider=KAKAO 일 때 사용",
                example = "4/0AX4XfWh..."
        )
        String code
) {
}
