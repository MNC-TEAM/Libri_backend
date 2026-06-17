package monochrome.libri.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "LoginApiResponse", description = "로그인 성공 응답")
public record LoginApiResponseDoc(
        @Schema(description = "성공 여부", example = "true")
        boolean success,
        @Schema(description = "공통 성공 코드", example = "OK")
        String code,
        @Schema(description = "성공 메시지", nullable = true, example = "null")
        String message,
        @Schema(description = "로그인 응답 데이터")
        LoginResponseDto data
) {
}
