package monochrome.libri.common.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

public class RequestIdMdcFilter extends OncePerRequestFilter {

    public static final String MDC_KEY = "requestId";
    public static final String HEADER = "X-Request-ID";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String requestId = resolveRequestId(request);
        boolean setByThisFilter = ensureMdcHasRequestId(requestId);     // MDC에 requestId가 없으면 넣고 true 반환

        // 응답 헤더에도 동일한 요청 ID 설정
        response.setHeader(HEADER, requestId);

        // 필터 체인 계속 진행
        try {
            filterChain.doFilter(request, response);
        } finally {
            if (setByThisFilter) {
                MDC.remove(MDC_KEY);
            }
        }
    }

    // 요청에서 X-Request-ID 헤더를 확인하고, 없으면 새로 생성한다.
    private String resolveRequestId(HttpServletRequest request) {
        String fromHeader = request.getHeader(HEADER);
        if (fromHeader != null && !fromHeader.isBlank()) {
            return fromHeader;
        }
        return UUID.randomUUID().toString();
    }

    // MDC에 이미 requestId가 있으면 false 반환, 없으면 넣고 true 반환
    private boolean ensureMdcHasRequestId(String requestId) {
        String existing = MDC.get(MDC_KEY);
        if (existing != null && !existing.isBlank()) {
            return false;
        }
        MDC.put(MDC_KEY, requestId);
        return true;
    }
}
