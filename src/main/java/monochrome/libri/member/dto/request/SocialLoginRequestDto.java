package monochrome.libri.member.dto.request;

import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;
import monochrome.libri.member.domain.SignType;

public record SocialLoginRequestDto(
        @Schema(
                description = "소셜 로그인 제공자. 현재 KAKAO, APPLE 지원",
                example = "KAKAO"
        )
        @NotNull(message = "소셜 로그인 제공자는 필수입니다.")
        SignType provider,
        @Schema(
                description = "카카오 로그인 후 프론트가 받은 access token. provider=KAKAO 일 때 사용",
                example = "kakao-access-token"
        )
        String accessToken,
        @Schema(
                description = "애플 로그인 후 프론트가 받은 id token(JWT). provider=APPLE 일 때 사용",
                example = "eyJraWQiOiJ...apple-id-token"
        )
        String idToken
) {
}
