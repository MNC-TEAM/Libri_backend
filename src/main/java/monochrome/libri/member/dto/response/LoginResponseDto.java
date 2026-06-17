package monochrome.libri.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record LoginResponseDto(
        @Schema(description = "토큰 타입", example = "Bearer")
        String tokenType,
        @Schema(description = "백엔드 access token", example = "ACCESS_TOKEN")
        String accessToken,
        @Schema(description = "백엔드 refresh token", example = "REFRESH_TOKEN")
        String refreshToken,
        @Schema(description = "로그인한 회원 정보")
        MemberResponseDto memberResponseDto
) {
    public static LoginResponseDto of(
            String tokenType,
            String accessToken,
            String refreshToken,
            MemberResponseDto memberResponseDto
    ) {
        return new LoginResponseDto(tokenType, accessToken, refreshToken, memberResponseDto);
    }
}
