package monochrome.libri.member.controller;

import monochrome.libri.global.exception.LibriException;
import monochrome.libri.global.security.token.JwtTokenService;
import monochrome.libri.member.config.SocialLoginProperties;
import monochrome.libri.member.domain.SignType;
import monochrome.libri.member.dto.request.SocialLoginRequestDto;
import monochrome.libri.member.dto.response.MemberResponseDto;
import monochrome.libri.member.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private JwtTokenService jwtTokenService;

    private AuthController controller;

    @BeforeEach
    void setUp() {
        controller = new AuthController(
                authService,
                jwtTokenService,
                new SocialLoginProperties(
                        new SocialLoginProperties.Kakao(
                                "https://kapi.kakao.com",
                                "https://kauth.kakao.com",
                                "client-id",
                                "client-secret",
                                "http://15.164.169.176/dev/api/v1/auth/login/kakao/callback"
                        ),
                        new SocialLoginProperties.Apple(
                                "https://appleid.apple.com",
                                "https://appleid.apple.com",
                                List.of()
                        )
                )
        );
    }

    @Test
    void redirectToKakaoLogin_returnsAuthorizeRedirect() {
        var response = controller.redirectToKakaoLogin();

        assertThat(response.getStatusCode().value()).isEqualTo(302);
        assertThat(response.getHeaders().getLocation()).hasToString(
                "https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=client-id&redirect_uri=http://15.164.169.176/dev/api/v1/auth/login/kakao/callback"
        );
    }

    @Test
    void loginByKakaoCallback_usesExistingSocialLoginFlow() {
        MemberResponseDto member = new MemberResponseDto(
                12L,
                SignType.KAKAO,
                "user@test.com",
                "nick12",
                "/profile/12",
                false,
                0L,
                0L
        );
        when(authService.loginBySocial(any())).thenReturn(member);

        var response = controller.loginByKakaoCallback("kakao-code");

        ArgumentCaptor<SocialLoginRequestDto> captor = ArgumentCaptor.forClass(SocialLoginRequestDto.class);
        verify(authService).loginBySocial(captor.capture());
        assertThat(captor.getValue().provider()).isEqualTo(SignType.KAKAO);
        assertThat(captor.getValue().accessToken()).isNull();
        assertThat(captor.getValue().code()).isEqualTo("kakao-code");
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data()).isNull();
    }

    @Test
    void loginByKakaoCallback_rejectsBlankCode() {
        assertThatThrownBy(() -> controller.loginByKakaoCallback(" "))
                .isInstanceOf(LibriException.class);
    }
}
