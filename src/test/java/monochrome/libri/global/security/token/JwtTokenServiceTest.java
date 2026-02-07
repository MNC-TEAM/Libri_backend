package monochrome.libri.global.security.token;

import monochrome.libri.global.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtTokenServiceTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private JwtTokenService service;

    @Test
    void issueAccessToken_returnsToken() {
        TokenIssueResult issued = new TokenIssueResult("token", Instant.parse("2024-01-01T00:00:00Z"), "jti");
        when(jwtTokenProvider.createAccessToken(1L)).thenReturn(issued);

        String token = service.issueAccessToken(1L);

        assertThat(token).isEqualTo("token");
    }
}
