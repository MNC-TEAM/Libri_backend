package monochrome.libri.member.social;

import monochrome.libri.global.security.oauth.kakao.KakaoOAuthClient;
import monochrome.libri.member.domain.SignType;
import monochrome.libri.member.dto.request.SocialLoginRequestDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KakaoSocialLoginProviderTest {

    @Mock
    private KakaoOAuthClient kakaoOAuthClient;

    @Test
    void authenticate_usesAccessTokenWhenProvided() {
        KakaoSocialLoginProvider provider = new KakaoSocialLoginProvider(kakaoOAuthClient);
        SocialLoginRequestDto request = new SocialLoginRequestDto(SignType.KAKAO, null, "kakao-access-token", null);
        KakaoSocialLoginProvider.KakaoUserResponse response = new KakaoSocialLoginProvider.KakaoUserResponse(
                123L,
                new KakaoSocialLoginProvider.KakaoAccount("user@test.com", true)
        );
        when(kakaoOAuthClient.getUserInfoByAccessToken("kakao-access-token")).thenReturn(response);

        SocialUserInfo userInfo = provider.authenticate(request);

        assertThat(userInfo.provider()).isEqualTo(SignType.KAKAO);
        assertThat(userInfo.providerUserId()).isEqualTo("123");
        assertThat(userInfo.email()).isEqualTo("user@test.com");
        assertThat(userInfo.emailVerified()).isTrue();
        verify(kakaoOAuthClient).getUserInfoByAccessToken("kakao-access-token");
    }

    @Test
    void authenticate_fallsBackToAuthorizationCode() {
        KakaoSocialLoginProvider provider = new KakaoSocialLoginProvider(kakaoOAuthClient);
        SocialLoginRequestDto request = new SocialLoginRequestDto(SignType.KAKAO, null, null, "kakao-code");
        KakaoSocialLoginProvider.KakaoUserResponse response = new KakaoSocialLoginProvider.KakaoUserResponse(
                456L,
                new KakaoSocialLoginProvider.KakaoAccount("user2@test.com", true)
        );
        when(kakaoOAuthClient.getUserInfo("kakao-code")).thenReturn(response);

        SocialUserInfo userInfo = provider.authenticate(request);

        assertThat(userInfo.providerUserId()).isEqualTo("456");
        assertThat(userInfo.email()).isEqualTo("user2@test.com");
        verify(kakaoOAuthClient).getUserInfo("kakao-code");
    }
}
