package monochrome.libri.member.social;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureException;
import monochrome.libri.global.exception.ErrorCode;
import monochrome.libri.global.exception.LibriException;
import monochrome.libri.member.config.SocialLoginProperties;
import monochrome.libri.member.domain.SignType;
import monochrome.libri.member.dto.request.SocialLoginRequestDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Set;

@Component
public class AppleSocialLoginProvider implements SocialLoginProvider {

    private final RestClient restClient;
    private final SocialLoginProperties properties;
    private final ObjectMapper objectMapper;

    public AppleSocialLoginProvider(
            @Qualifier("appleSocialRestClient") RestClient restClient,
            SocialLoginProperties properties,
            ObjectMapper objectMapper
    ) {
        this.restClient = restClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public SignType provider() {
        return SignType.APPLE;
    }

    @Override
    public SocialUserInfo authenticate(SocialLoginRequestDto request) {
        String idToken = normalize(request.idToken());
        if (idToken == null) {
            throw new LibriException(ErrorCode.INVALID_INPUT_VALUE);
        }

        AppleTokenHeader header = parseHeader(idToken);
        AppleJwk key = fetchSigningKey(header.kid(), header.alg());
        Claims claims = parseClaims(idToken, key);
        validateClaims(claims);

        return new SocialUserInfo(
                SignType.APPLE,
                claims.getSubject(),
                normalize(claims.get("email", String.class)),
                toBoolean(claims.get("email_verified"))
        );
    }

    private AppleTokenHeader parseHeader(String idToken) {
        try {
            String[] parts = idToken.split("\\.");
            if (parts.length != 3) {
                throw new LibriException(ErrorCode.INVALID_SOCIAL_TOKEN);
            }
            byte[] decoded = Base64.getUrlDecoder().decode(parts[0]);
            return objectMapper.readValue(decoded, AppleTokenHeader.class);
        } catch (Exception e) {
            throw new LibriException("애플 토큰 헤더를 파싱할 수 없습니다.", e, ErrorCode.INVALID_SOCIAL_TOKEN);
        }
    }

    private AppleJwk fetchSigningKey(String kid, String alg) {
        AppleJwkSetResponse response;
        try {
            response = restClient.get()
                    .uri("/auth/keys")
                    .retrieve()
                    .body(AppleJwkSetResponse.class);
        } catch (RestClientException e) {
            throw new LibriException("애플 공개키를 조회할 수 없습니다.", e, ErrorCode.INVALID_SOCIAL_TOKEN);
        }

        if (response == null || response.keys() == null) {
            throw new LibriException(ErrorCode.INVALID_SOCIAL_TOKEN);
        }

        return response.keys().stream()
                .filter(key -> kid.equals(key.kid()) && alg.equals(key.alg()))
                .findFirst()
                .orElseThrow(() -> new LibriException(ErrorCode.INVALID_SOCIAL_TOKEN));
    }

    private Claims parseClaims(String idToken, AppleJwk jwk) {
        try {
            return Jwts.parser()
                    .verifyWith(toPublicKey(jwk))
                    .build()
                    .parseSignedClaims(idToken)
                    .getPayload();
        } catch (SignatureException | IllegalArgumentException e) {
            throw new LibriException("애플 토큰 검증에 실패했습니다.", e, ErrorCode.INVALID_SOCIAL_TOKEN);
        }
    }

    private RSAPublicKey toPublicKey(AppleJwk jwk) {
        try {
            byte[] modulusBytes = Base64.getUrlDecoder().decode(jwk.n());
            byte[] exponentBytes = Base64.getUrlDecoder().decode(jwk.e());
            RSAPublicKeySpec keySpec = new RSAPublicKeySpec(
                    new BigInteger(1, modulusBytes),
                    new BigInteger(1, exponentBytes)
            );
            return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(keySpec);
        } catch (Exception e) {
            throw new LibriException("애플 공개키 생성에 실패했습니다.", e, ErrorCode.INVALID_SOCIAL_TOKEN);
        }
    }

    private void validateClaims(Claims claims) {
        if (claims.getSubject() == null || claims.getSubject().isBlank()) {
            throw new LibriException(ErrorCode.INVALID_SOCIAL_TOKEN);
        }

        String issuer = claims.getIssuer();
        if (!properties.apple().issuer().equals(issuer)) {
            throw new LibriException(ErrorCode.INVALID_SOCIAL_TOKEN);
        }

        List<String> clientIds = properties.apple().clientIds();
        Set<String> audiences = claims.getAudience();
        if (clientIds == null || clientIds.isEmpty() || audiences == null || audiences.stream().noneMatch(clientIds::contains)) {
            throw new LibriException(ErrorCode.INVALID_SOCIAL_TOKEN);
        }
    }

    private boolean toBoolean(Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value instanceof String text) {
            return "true".equalsIgnoreCase(text);
        }
        return false;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record AppleTokenHeader(
            String kid,
            String alg
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record AppleJwkSetResponse(
            List<AppleJwk> keys
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record AppleJwk(
            String kty,
            String kid,
            String use,
            String alg,
            String n,
            String e
    ) {
    }
}
