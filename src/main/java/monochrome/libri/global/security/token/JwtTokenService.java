package monochrome.libri.global.security.token;

import monochrome.libri.global.security.jwt.JwtTokenProvider;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtTokenService(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public String issueAccessToken(long memberId) {
        return jwtTokenProvider.createAccessToken(memberId);
    }
}
