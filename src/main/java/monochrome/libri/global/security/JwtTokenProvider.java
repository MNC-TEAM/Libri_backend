package monochrome.libri.global.security;

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
import java.util.Map;

@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long accessExpMs;
    private final String issuer;
    private final Clock clock;

    public JwtTokenProvider(
            @Value("${jwt.secret") String key,
            @Value("${jwt.access-exp-seconds") long accessExpMs,
            @Value("${jwt.issuer") String issuer,
            Clock clock
    ) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(key));
        this.accessExpMs = accessExpMs;
        this.issuer = issuer;
        this.clock = clock;
    }

    public String createAccessToken(long memberId, String role) {
        Instant now = clock.instant();
        Instant exp = now.plusMillis(accessExpMs);

        return Jwts.builder()
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .subject(String.valueOf(memberId))          // 대표 식별자
                .claims(Map.of("role", role))           // 추가 정보
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public Jws<Claims> parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .requireId(issuer)
                .build()
                .parseSignedClaims(token);
    }

    public boolean isValid(String token) {
        try {
            parse(token);
            return true;
        } catch(JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public long getMemberId(String token) {
        return Long.parseLong(parse(token).getPayload().getSubject());
    }

    public String getRole(String token) {
        Object role = parse(token).getPayload().get("role");
        return role == null ? null : String.valueOf(role);
    }

}
