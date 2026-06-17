package monochrome.libri.member.social;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import monochrome.libri.global.exception.ErrorCode;
import monochrome.libri.global.exception.LibriException;
import monochrome.libri.member.domain.SignType;
import monochrome.libri.member.dto.request.SocialLoginRequestDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class KakaoSocialLoginProvider implements SocialLoginProvider {

    private final RestClient restClient;

    public KakaoSocialLoginProvider(@Qualifier("kakaoSocialRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public SignType provider() {
        return SignType.KAKAO;
    }

    @Override
    public SocialUserInfo authenticate(SocialLoginRequestDto request) {
        String accessToken = normalize(request.accessToken());
        if (accessToken == null) {
            throw new LibriException(ErrorCode.INVALID_INPUT_VALUE);
        }

        KakaoUserResponse response;
        try {
            response = restClient.get()
                    .uri("/v2/user/me")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .body(KakaoUserResponse.class);
        } catch (RestClientException e) {
            throw new LibriException("카카오 사용자 조회에 실패했습니다.", e, ErrorCode.INVALID_SOCIAL_TOKEN);
        }

        if (response == null || response.id() == null) {
            throw new LibriException(ErrorCode.INVALID_SOCIAL_TOKEN);
        }

        String email = null;
        boolean emailVerified = false;
        if (response.kakaoAccount() != null) {
            email = normalize(response.kakaoAccount().email());
            emailVerified = Boolean.TRUE.equals(response.kakaoAccount().isEmailVerified());
        }

        return new SocialUserInfo(
                SignType.KAKAO,
                String.valueOf(response.id()),
                email,
                emailVerified
        );
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record KakaoUserResponse(
            Long id,
            KakaoAccount kakaoAccount
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record KakaoAccount(
            String email,
            Boolean isEmailVerified
    ) {
    }
}
