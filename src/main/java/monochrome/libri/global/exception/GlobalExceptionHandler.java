package monochrome.libri.global.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import monochrome.libri.global.response.ApiResponse;
import monochrome.libri.global.response.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Locale;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(LibriException.class)
    public ResponseEntity<ApiResponse<Void>> handleLibriException(LibriException e, HttpServletRequest req) {
        ErrorCode errorCode = e.getErrorCode();

        // 인증 사용자면 memberId(principal), 아니면 ip/ua로 식별
        String actor = resolveActor();

        // 비즈니스 예외: 스택트레이스는 보통 불필요(노이즈↓)
        log.warn("error.libri code={} method={} uri={} actor={} ip={} ua={} msg=\"{}\"",
                errorCode.getCode(),
                req.getMethod(),
                req.getRequestURI(),
                actor,
                resolveClientIp(req),
                shorten(req.getHeader("User-Agent"), 60),
                e.getMessage()
        );

//        log.warn("[LibriException] code={}, message={}",
//                errorCode.getCode(), e.getMessage());

        // e.getMessage()에 커스텀 메시지를 넣었다면 그걸 사용
        ErrorResponse body = ErrorResponse.from(errorCode, e.getMessage());

        return buildErrorResponse(errorCode, body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException e, HttpServletRequest req) {
        FieldError fieldError = e.getBindingResult().getFieldErrors().stream().findFirst().orElse(null);
        ErrorCode errorCode = mapFieldError(fieldError);
        return buildErrorResponse(errorCode, ErrorResponse.from(errorCode));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Void>> handleBindException(BindException e, HttpServletRequest req) {
        FieldError fieldError = e.getBindingResult().getFieldErrors().stream().findFirst().orElse(null);
        ErrorCode errorCode = mapFieldError(fieldError);
        return buildErrorResponse(errorCode, ErrorResponse.from(errorCode));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException e, HttpServletRequest req) {
        ConstraintViolation<?> violation = e.getConstraintViolations().stream().findFirst().orElse(null);
        ErrorCode errorCode = mapConstraintViolation(violation);
        return buildErrorResponse(errorCode, ErrorResponse.from(errorCode));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException e, HttpServletRequest req) {
        ErrorCode errorCode = mapTypeMismatch(e);
        return buildErrorResponse(errorCode, ErrorResponse.from(errorCode));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleMessageNotReadable(HttpMessageNotReadableException e, HttpServletRequest req) {
        ErrorCode errorCode = mapMessageNotReadable(e);
        return buildErrorResponse(errorCode, ErrorResponse.from(errorCode));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e, HttpServletRequest req) {
//        log.error("[Exception] {}", e.getMessage(), e);

        String actor = resolveActor();

        // 예상치 못한 예외: 스택트레이스 포함(원인 분석)
        log.error("[Exception] method={} uri={} actor={} message={}",
                req.getMethod(),
                req.getRequestURI(),
                actor,
                e.getMessage(),
                e
        );

        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        ErrorResponse body = ErrorResponse.from(errorCode);

        return buildErrorResponse(errorCode, body);
    }

    private ResponseEntity<ApiResponse<Void>> buildErrorResponse(ErrorCode errorCode, ErrorResponse body) {
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.error(body));
    }

    private ErrorCode mapFieldError(FieldError fieldError) {
        if (fieldError == null) {
            return ErrorCode.INVALID_INPUT_VALUE;
        }

        String field = fieldError.getField();
        String rule = fieldError.getCode();
        String objectName = fieldError.getObjectName();
        String normalizedRule = rule == null ? "" : rule.toLowerCase(Locale.ROOT);

        if ("rawPassword".equals(field) && "notblank".equals(normalizedRule)) {
            return ErrorCode.PASSWORD_TOO_SHORT;
        }

        if ("rawPassword".equals(field) && "size".equals(normalizedRule)) {
            Object rejected = fieldError.getRejectedValue();
            if (rejected instanceof String password) {
                if (password.length() < 8) {
                    return ErrorCode.PASSWORD_TOO_SHORT;
                }
                if (password.length() > 100) {
                    return ErrorCode.PASSWORD_TOO_LONG;
                }
            }
            return ErrorCode.INVALID_INPUT_VALUE;
        }

        if ("email".equals(field)
                && ("email".equals(normalizedRule) || "pattern".equals(normalizedRule) || "notblank".equals(normalizedRule))) {
            return ErrorCode.EMAIL_FORMAT_INVALID;
        }

        if ("nickname".equals(field) && ("size".equals(normalizedRule) || "notblank".equals(normalizedRule))) {
            return ErrorCode.NICKNAME_LENGTH_INVALID;
        }

        if ("rating".equals(field) && ("min".equals(normalizedRule) || "max".equals(normalizedRule) || "notnull".equals(normalizedRule))) {
            return ErrorCode.REVIEW_RATING_OUT_OF_RANGE;
        }

        if ("publisher".equals(field) && "size".equals(normalizedRule)) {
            return ErrorCode.BOOK_PUBLISHER_TOO_LONG;
        }

        if ("content".equals(field) && "size".equals(normalizedRule)) {
            if (objectName != null && objectName.toLowerCase(Locale.ROOT).contains("note")) {
                return ErrorCode.NOTE_CONTENT_TOO_LONG;
            }
            return ErrorCode.CONTENT_TOO_LONG;
        }

        return ErrorCode.INVALID_INPUT_VALUE;
    }

    private ErrorCode mapConstraintViolation(ConstraintViolation<?> violation) {
        if (violation == null || violation.getPropertyPath() == null) {
            return ErrorCode.INVALID_INPUT_VALUE;
        }

        String path = violation.getPropertyPath().toString();
        if (path.endsWith("page") || path.endsWith("size")) {
            return ErrorCode.PAGINATION_INVALID;
        }
        return ErrorCode.INVALID_INPUT_VALUE;
    }

    private ErrorCode mapTypeMismatch(MethodArgumentTypeMismatchException e) {
        String name = e.getName();
        if ("page".equals(name) || "size".equals(name)) {
            return ErrorCode.PAGINATION_INVALID;
        }

        Class<?> requiredType = e.getRequiredType();
        if (requiredType != null && requiredType.isEnum()) {
            return ErrorCode.ENUM_VALUE_INVALID;
        }
        return ErrorCode.REQUEST_FORMAT_INVALID;
    }

    private ErrorCode mapMessageNotReadable(HttpMessageNotReadableException e) {
        Throwable cause = e.getCause();
        if (cause instanceof InvalidFormatException ife) {
            Class<?> targetType = ife.getTargetType();
            if (targetType != null && targetType.isEnum()) {
                return ErrorCode.ENUM_VALUE_INVALID;
            }
        }
        return ErrorCode.REQUEST_FORMAT_INVALID;
    }


    /**
     * 인증 사용자: principal(auth.getName()) (너는 memberId로 사용한다고 했으니 그대로 활용)
     * 미인증 사용자: ip + user-agent로 대략 식별
     */
    private String resolveActor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            String name = auth.getName();
            if (name != null && !name.isBlank() && !"anonymousUser".equals(name)) {
                return "memberId:" + name;
            }
        }
        return "anonymous";
    }

    private String resolveClientIp(HttpServletRequest req) {
        // Nginx/프록시 환경이면 X-Forwarded-For 첫 값이 원 IP일 가능성이 큼
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        String xri = req.getHeader("X-Real-IP");
        if (xri != null && !xri.isBlank()) {
            return xri.trim();
        }
        return req.getRemoteAddr();
    }

    private String shorten(String s, int max) {
        if (s == null || s.isBlank()) return "-";
        return (s.length() <= max) ? s : s.substring(0, max) + "...";
    }
}
