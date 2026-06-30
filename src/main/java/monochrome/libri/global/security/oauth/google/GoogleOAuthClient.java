package monochrome.libri.global.security.oauth.google;

import lombok.extern.slf4j.Slf4j;
import monochrome.libri.global.exception.ErrorCode;
import monochrome.libri.global.exception.LibriException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
public class GoogleOAuthClient {

    private static final String TOKENINFO_URL = "https://oauth2.googleapis.com/tokeninfo";

    private final RestTemplate restTemplate;
    private final String clientId;

    public GoogleOAuthClient(@Value("${google.client-id}") String clientId) {
        this.restTemplate = new RestTemplate();
        this.clientId = clientId;
    }

    public GoogleUserInfo verifyIdToken(String idToken) {
        String url = UriComponentsBuilder.fromHttpUrl(TOKENINFO_URL)
                .queryParam("id_token", idToken)
                .toUriString();

        try {
            ResponseEntity<GoogleTokenInfoResponse> response =
                    restTemplate.getForEntity(url, GoogleTokenInfoResponse.class);
            GoogleTokenInfoResponse body = response.getBody();
            if (body == null) {
                throw new LibriException(ErrorCode.GOOGLE_TOKEN_INVALID);
            }
            if (!clientId.equals(body.aud())) {
                log.warn("auth.google.token.verify aud_mismatch expected={} actual={}", clientId, body.aud());
                throw new LibriException(ErrorCode.GOOGLE_TOKEN_INVALID);
            }
            return new GoogleUserInfo(body.sub(), body.email(), "true".equals(body.emailVerified()), body.name(), body.picture());
        } catch (LibriException e) {
            throw e;
        } catch (RestClientException e) {
            log.warn("auth.google.token.verify result=fail error={}", e.getMessage());
            throw new LibriException(ErrorCode.GOOGLE_TOKEN_INVALID);
        }
    }
}
