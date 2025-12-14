package monochrome.libri.global.response;

import monochrome.libri.global.exception.ErrorCode;

/**
 * 에러 응답 공통 포맷
 * - 추후 공통 Response<T> 래핑 시, 실패 응답의 payload로 사용할 예정
 */
public record ErrorResponse (
        String code,        // 비지니스 에러 코드
        String message      // 에러 메시지
){
    /**
     * ErrorCode의 기본 메시지를 사용하는 경우.
     */
    public static ErrorResponse from(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.getCode(), errorCode.getMessage());
    }

    /**
     * ErrorCode는 같지만, 메시지를 상세하게 커스터마이징하고 싶은 경우
     * thread id, timestamp 등
     */
    public static ErrorResponse from(ErrorCode errorCode, String message) {
        return new ErrorResponse(errorCode.getCode(), message);
    }
}
