package monochrome.libri.config;

import monochrome.libri.common.logging.RequestIdMdcFilter;
import monochrome.libri.global.security.jwt.JwtAuthenticationFilter;
import monochrome.libri.global.security.jwt.JwtTokenProvider;
import monochrome.libri.global.security.web.CustomAccessDeniedHandler;
import monochrome.libri.global.security.web.CustomAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class securityConfig {

    @Bean
    public RequestIdMdcFilter requestIdMdcFilter() {
        return new RequestIdMdcFilter();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(
            JwtTokenProvider jwtTokenProvider,
            UserDetailsService userDetailsService
    ) {
        return new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            CustomAuthenticationEntryPoint entryPoint,              // 인증이 필요한 리소스에 인증되지 않은 사용자가 접근할 때 처리(401)
            CustomAccessDeniedHandler accessDeniedHandler,          // 인증된 사용자가 권한이 없는 리소스에 접근할 때 처리(403)
            RequestIdMdcFilter requestIdMdcFilter,                  // 요청 ID 필터
            JwtAuthenticationFilter jwtAuthenticationFilter         // JWT 인증 필터
            ) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm ->sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))      // JWT 사용 시 세션 미사용
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(entryPoint)           // 401 처리 핸들러
                        .accessDeniedHandler(accessDeniedHandler)       // 403 처리 핸들러
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/v1/members/**").permitAll()
                        .anyRequest().permitAll()
                )
                // UsernamePasswordAuthenticationFilter 전에 JWT 필터를 추가
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                )
                .addFilterBefore(
                        requestIdMdcFilter,
                        JwtAuthenticationFilter.class
                )
        ;


        return http.build();
    }
}
