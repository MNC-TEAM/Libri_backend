package monochrome.libri.global.swagger;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 기존 ApiResponse<T> 대신 사용
 * 제네릭 타입이라서 뭐가 오냐에 따라 이름이 바뀌기 떄문에 문서용으로 별도 작성
 * ex) ApiResponse<MemberResponse>, ApiResponse<List<MemberResponse>> 등
 * 실패 응답은 항상 동일(data는 null)이므로 제네릭 불필요
 * OpenApiServiceErrorCustomizerConfig 에서 $ref로 참조됨
 */

@Schema(name = "ApiErrorResponse", description = "공통 실패 응답(문서용)")
public record ApiErrorResponse(
        @Schema(example = "false") boolean success,
        @Schema(example = "M001") String code,
        @Schema(example = "존재하지 않는 회원입니다.") String message,
        @Schema(description = "항상 null") Object data
) {}
