package monochrome.libri.global.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import monochrome.libri.global.response.ApiResponse;
import monochrome.libri.global.response.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.error(body));
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

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.error(body));
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
