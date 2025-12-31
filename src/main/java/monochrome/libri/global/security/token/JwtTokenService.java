package monochrome.libri.global.security.token;

import lombok.extern.slf4j.Slf4j;
import monochrome.libri.global.security.jwt.JwtTokenProvider;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class JwtTokenService {
    private final JwtTokenProvider jwtTokenProvider;

    public JwtTokenService(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public String issueAccessToken(long memberId) {

        TokenIssueResult issued = jwtTokenProvider.createAccessToken(memberId);

        log.info("auth.jwt.issue type=access memberId={} exp={} jti={}",
                memberId, issued.expiresAt(), issued.jti());

        return issued.token();
    }
}
