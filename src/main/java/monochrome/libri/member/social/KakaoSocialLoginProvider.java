package monochrome.libri.member.social;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import monochrome.libri.global.security.oauth.kakao.KakaoOAuthClient;
import monochrome.libri.member.domain.SignType;
import monochrome.libri.member.dto.request.SocialLoginRequestDto;
import org.springframework.stereotype.Component;

@Component
public class KakaoSocialLoginProvider implements SocialLoginProvider {

    private final KakaoOAuthClient kakaoOAuthClient;

    public KakaoSocialLoginProvider(KakaoOAuthClient kakaoOAuthClient) {
        this.kakaoOAuthClient = kakaoOAuthClient;
    }

    @Override
    public SignType provider() {
        return SignType.KAKAO;
    }

    @Override
    public SocialUserInfo authenticate(SocialLoginRequestDto request) {
        KakaoUserResponse response = hasText(request.accessToken())
                ? kakaoOAuthClient.getUserInfoByAccessToken(request.accessToken())
                : kakaoOAuthClient.getUserInfo(request.code());

        if (response == null || response.id() == null) {
            throw kakaoOAuthClient.invalidSocialToken();
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

    private boolean hasText(String value) {
        return normalize(value) != null;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record KakaoUserResponse(
            Long id,
            KakaoAccount kakaoAccount
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record KakaoAccount(
            String email,
            Boolean isEmailVerified
    ) {
    }
}
