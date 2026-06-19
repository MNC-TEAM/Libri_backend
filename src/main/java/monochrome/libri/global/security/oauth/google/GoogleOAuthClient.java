package monochrome.libri.global.security.oauth.google;

import lombok.extern.slf4j.Slf4j;
import monochrome.libri.global.exception.ErrorCode;
import monochrome.libri.global.exception.LibriException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class GoogleOAuthClient {

    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String USERINFO_URL = "https://www.googleapis.com/userinfo/v2/me";

    private final RestTemplate restTemplate;
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;

    public GoogleOAuthClient(
            @Value("${google.client-id}") String clientId,
            @Value("${google.client-secret}") String clientSecret,
            @Value("${google.redirect-uri}") String redirectUri
    ) {
        this.restTemplate = new RestTemplate();
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
    }

    public GoogleUserInfo getUserInfo(String code) {
        String accessToken = exchangeCodeForAccessToken(code);
        return fetchUserInfo(accessToken);
    }

    private String exchangeCodeForAccessToken(String code) {
        String decodedCode = URLDecoder.decode(code, StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", decodedCode);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("grant_type", "authorization_code");

        try {
            ResponseEntity<GoogleTokenResponse> response = restTemplate.postForEntity(
                    TOKEN_URL,
                    new HttpEntity<>(params, headers),
                    GoogleTokenResponse.class
            );
            if (response.getBody() == null || response.getBody().accessToken() == null) {
                throw new LibriException(ErrorCode.GOOGLE_TOKEN_INVALID);
            }
            return response.getBody().accessToken();
        } catch (LibriException e) {
            throw e;
        } catch (RestClientException e) {
            log.warn("auth.google.token.exchange result=fail error={}", e.getMessage());
            throw new LibriException(ErrorCode.GOOGLE_TOKEN_INVALID);
        }
    }

    private GoogleUserInfo fetchUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        try {
            ResponseEntity<GoogleUserInfo> response = restTemplate.exchange(
                    USERINFO_URL,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    GoogleUserInfo.class
            );
            if (response.getBody() == null) {
                throw new LibriException(ErrorCode.GOOGLE_TOKEN_INVALID);
            }
            return response.getBody();
        } catch (LibriException e) {
            throw e;
        } catch (RestClientException e) {
            log.warn("auth.google.userinfo result=fail error={}", e.getMessage());
            throw new LibriException(ErrorCode.GOOGLE_TOKEN_INVALID);
        }
    }
}
