package monochrome.libri.global.exception;

import lombok.extern.slf4j.Slf4j;
import monochrome.libri.global.response.ApiResponse;
import monochrome.libri.global.response.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(LibriException.class)
    public ResponseEntity<ApiResponse<Void>> handleLibriException(LibriException e) {
        ErrorCode errorCode = e.getErrorCode();

        log.warn("[LibriException] code={}, message={}",
                errorCode.getCode(), e.getMessage());

        // e.getMessage()에 커스텀 메시지를 넣었다면 그걸 사용
        ErrorResponse body = ErrorResponse.from(errorCode, e.getMessage());

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.error(body));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("[Exception] {}", e.getMessage(), e);

        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        ErrorResponse body = ErrorResponse.from(errorCode);

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.error(body));
    }
}
