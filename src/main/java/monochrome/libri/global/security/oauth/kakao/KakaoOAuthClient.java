package monochrome.libri.global.security.oauth.kakao;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import monochrome.libri.global.exception.ErrorCode;
import monochrome.libri.global.exception.LibriException;
import monochrome.libri.member.config.SocialLoginProperties;
import monochrome.libri.member.social.KakaoSocialLoginProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Component
public class KakaoOAuthClient {

    private final RestClient authRestClient;
    private final RestClient apiRestClient;
    private final SocialLoginProperties.Kakao properties;

    public KakaoOAuthClient(
            @Qualifier("kakaoAuthRestClient") RestClient authRestClient,
            @Qualifier("kakaoSocialRestClient") RestClient apiRestClient,
            SocialLoginProperties socialLoginProperties
    ) {
        this.authRestClient = authRestClient;
        this.apiRestClient = apiRestClient;
        this.properties = socialLoginProperties.kakao();
    }

    public KakaoSocialLoginProvider.KakaoUserResponse getUserInfo(String code) {
        String authorizationCode = normalize(code);
        if (authorizationCode == null) {
            throw new LibriException(ErrorCode.INVALID_INPUT_VALUE);
        }

        String accessToken = exchangeCodeForAccessToken(authorizationCode);
        return fetchUserInfo(accessToken);
    }

    public LibriException invalidSocialToken() {
        return new LibriException(ErrorCode.INVALID_SOCIAL_TOKEN);
    }

    private String exchangeCodeForAccessToken(String code) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", properties.clientId());
        params.add("redirect_uri", properties.redirectUri());
        params.add("code", code);
        if (hasText(properties.clientSecret())) {
            params.add("client_secret", properties.clientSecret());
        }

        try {
            KakaoTokenResponse response = authRestClient.post()
                    .uri("/oauth/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(params)
                    .retrieve()
                    .body(KakaoTokenResponse.class);
            if (response == null || !hasText(response.accessToken())) {
                throw invalidSocialToken();
            }
            return response.accessToken();
        } catch (LibriException e) {
            throw e;
        } catch (RestClientException e) {
            log.warn("auth.kakao.token.exchange result=fail error={}", e.getMessage());
            throw new LibriException("카카오 토큰 교환에 실패했습니다.", e, ErrorCode.INVALID_SOCIAL_TOKEN);
        }
    }

    private KakaoSocialLoginProvider.KakaoUserResponse fetchUserInfo(String accessToken) {
        try {
            KakaoSocialLoginProvider.KakaoUserResponse response = apiRestClient.get()
                    .uri("/v2/user/me")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .body(KakaoSocialLoginProvider.KakaoUserResponse.class);
            if (response == null || response.id() == null) {
                throw invalidSocialToken();
            }
            return response;
        } catch (LibriException e) {
            throw e;
        } catch (RestClientException e) {
            log.warn("auth.kakao.userinfo result=fail error={}", e.getMessage());
            throw new LibriException("카카오 사용자 조회에 실패했습니다.", e, ErrorCode.INVALID_SOCIAL_TOKEN);
        }
    }

    private boolean hasText(String value) {
        return normalize(value) != null;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private record KakaoTokenResponse(
            @JsonProperty("access_token")
            String accessToken
    ) {
    }
}
