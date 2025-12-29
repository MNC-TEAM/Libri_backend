package monochrome.libri.global.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long accessExpMs;
    private final String issuer;
    private final Clock clock;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String key,
            @Value("${jwt.access-exp-seconds}") long accessExpMs,
            @Value("${jwt.issuer}") String issuer,
            Clock clock
    ) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(key));
        this.accessExpMs = accessExpMs;
        this.issuer = issuer;
        this.clock = clock;
    }

    public String createAccessToken(long memberId) {
        Instant now = clock.instant();
        Instant exp = now.plusMillis(accessExpMs);

        return Jwts.builder()
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .subject(String.valueOf(memberId))          // 대표 식별자
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public Jws<Claims> parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token);
    }

    public boolean isValid(String token) {
        try {
            parse(token);
            return true;
        } catch(JwtException | IllegalArgumentException e) {                // JWTException: 파싱 오류, 만료 등 모든 JWT 관련 예외의 부모 클래스, IllegalArgumentException: 토큰이 null or 빈 문자열인 경우 발생
            return false;
        }
    }

    public long getMemberId(String token) {
        return Long.parseLong(parse(token).getPayload().getSubject());
    }
}
