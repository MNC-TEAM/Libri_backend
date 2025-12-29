package monochrome.libri.global.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserDetailsService userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        // 토큰이 없으면: 익명으로 그냥 통과(공개 API 정책)
        if(header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7); // "Bearer " 이후 토큰 부분만 추출

        // 토큰이 있는데 유효하지 않으면: 인증 주입 안하고 통과
        // =>  공개 API는 그대로 동작, 보호 API는 나중에 401 처리
        if(!jwtTokenProvider.isValid(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 이미 인증이 있으면 중복 주입 방지
        if(SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        long memberId = jwtTokenProvider.getMemberId(token);

        UserDetails userDetails = userDetailsService.loadUserByUsername(String.valueOf(memberId));              // customUserDetailsService를 통해 DB에서 회원 정보 조회

        /*
          인증 주입 과정(SecurityContextHolder 인증 정보 저장)
          UsernamePasswordAuthenticationToken 인자로
          userDetails: principal(인증 주체)
          Credentials(자격 증명): null(이미 토큰으로 인증된 상태이므로 비밀번호는 필요 없음)
         authorities: userDetails.getAuthorities()(사용자 권한, 내가 DB에서 조회한 member의 권한)
         */
        AbstractAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );

        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));                  // 인증 정보에 요청 정보 추가(IP 주소 등)
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}
