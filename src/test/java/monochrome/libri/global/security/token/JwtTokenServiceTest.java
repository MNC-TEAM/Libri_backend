package monochrome.libri.global.security.token;

import monochrome.libri.global.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtTokenServiceTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RefreshTokenStore refreshTokenStore;

    @InjectMocks
    private JwtTokenService service;

    @Test
    void issueAccessToken_returnsToken() {
        TokenIssueResult issued = new TokenIssueResult("token", Instant.parse("2024-01-01T00:00:00Z"), "jti");
        when(jwtTokenProvider.createAccessToken(1L)).thenReturn(issued);

        String token = service.issueAccessToken(1L);

        assertThat(token).isEqualTo("token");
    }

    @Test
    @DisplayName("토큰 쌍 발급 시 refresh token을 저장한다")
    void issueTokenPair_savesRefreshToken() {
        TokenIssueResult accessIssued = new TokenIssueResult("access", Instant.parse("2024-01-01T00:00:00Z"), "a-jti");
        TokenIssueResult refreshIssued = new TokenIssueResult("refresh", Instant.parse("2024-01-08T00:00:00Z"), "r-jti");
        when(jwtTokenProvider.createAccessToken(1L)).thenReturn(accessIssued);
        when(jwtTokenProvider.createRefreshToken(1L)).thenReturn(refreshIssued);
        when(jwtTokenProvider.getRefreshExpSeconds()).thenReturn(604800L);

        TokenPair tokenPair = service.issueTokenPair(1L);

        assertThat(tokenPair.accessToken()).isEqualTo("access");
        assertThat(tokenPair.refreshToken()).isEqualTo("refresh");
        verify(refreshTokenStore).save(1L, "refresh", 604800L);
    }

    @Test
    @DisplayName("유효한 refresh token이면 새 토큰 쌍을 발급한다")
    void refresh_reissuesTokenPair() {
        TokenIssueResult accessIssued = new TokenIssueResult("new-access", Instant.parse("2024-01-01T00:00:00Z"), "a-jti");
        TokenIssueResult refreshIssued = new TokenIssueResult("new-refresh", Instant.parse("2024-01-08T00:00:00Z"), "r-jti");
        when(jwtTokenProvider.isValid("refresh")).thenReturn(true);
        when(jwtTokenProvider.getTokenType("refresh")).thenReturn(TokenType.REFRESH);
        when(jwtTokenProvider.getMemberId("refresh")).thenReturn(1L);
        when(refreshTokenStore.matches(1L, "refresh")).thenReturn(true);
        when(jwtTokenProvider.createAccessToken(1L)).thenReturn(accessIssued);
        when(jwtTokenProvider.createRefreshToken(1L)).thenReturn(refreshIssued);
        when(jwtTokenProvider.getRefreshExpSeconds()).thenReturn(604800L);

        TokenPair tokenPair = service.refresh("refresh");

        assertThat(tokenPair.accessToken()).isEqualTo("new-access");
        assertThat(tokenPair.refreshToken()).isEqualTo("new-refresh");
        verify(refreshTokenStore).save(1L, "new-refresh", 604800L);
    }

    @Test
    @DisplayName("저장소에 없는 refresh token은 거절한다")
    void refresh_rejectsUnknownToken() {
        when(jwtTokenProvider.isValid("refresh")).thenReturn(true);
        when(jwtTokenProvider.getTokenType("refresh")).thenReturn(TokenType.REFRESH);
        when(jwtTokenProvider.getMemberId("refresh")).thenReturn(1L);
        when(refreshTokenStore.matches(1L, "refresh")).thenReturn(false);

        assertThatThrownBy(() -> service.refresh("refresh"))
                .isInstanceOf(monochrome.libri.global.exception.LibriException.class);
    }
}
