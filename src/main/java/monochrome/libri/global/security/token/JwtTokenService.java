package monochrome.libri.global.security.token;

import lombok.extern.slf4j.Slf4j;
import monochrome.libri.global.exception.ErrorCode;
import monochrome.libri.global.exception.LibriException;
import monochrome.libri.global.security.jwt.JwtTokenProvider;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class JwtTokenService {
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenStore refreshTokenStore;

    public JwtTokenService(JwtTokenProvider jwtTokenProvider, RefreshTokenStore refreshTokenStore) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenStore = refreshTokenStore;
    }

    public String issueAccessToken(long memberId) {

        TokenIssueResult issued = jwtTokenProvider.createAccessToken(memberId);

        log.info("auth.jwt.issue type=access memberId={} exp={} jti={}",
                memberId, issued.expiresAt(), issued.jti());

        return issued.token();
    }

    public TokenPair issueTokenPair(long memberId) {
        TokenIssueResult accessIssued = jwtTokenProvider.createAccessToken(memberId);
        TokenIssueResult refreshIssued = jwtTokenProvider.createRefreshToken(memberId);

        refreshTokenStore.save(memberId, refreshIssued.token(), jwtTokenProvider.getRefreshExpSeconds());

        log.info("auth.jwt.issue type=access memberId={} exp={} jti={}",
                memberId, accessIssued.expiresAt(), accessIssued.jti());
        log.info("auth.jwt.issue type=refresh memberId={} exp={} jti={}",
                memberId, refreshIssued.expiresAt(), refreshIssued.jti());

        return TokenPair.bearer(accessIssued.token(), refreshIssued.token());
    }

    public TokenPair refresh(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new LibriException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        if (!jwtTokenProvider.isValid(refreshToken)) {
            throw new LibriException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        if (jwtTokenProvider.getTokenType(refreshToken) != TokenType.REFRESH) {
            throw new LibriException(ErrorCode.TOKEN_TYPE_MISMATCH);
        }

        long memberId = jwtTokenProvider.getMemberId(refreshToken);
        if (!refreshTokenStore.matches(memberId, refreshToken)) {
            throw new LibriException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        return issueTokenPair(memberId);
    }

    public void revokeRefreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new LibriException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        if (!jwtTokenProvider.isValid(refreshToken)) {
            throw new LibriException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        if (jwtTokenProvider.getTokenType(refreshToken) != TokenType.REFRESH) {
            throw new LibriException(ErrorCode.TOKEN_TYPE_MISMATCH);
        }

        long memberId = jwtTokenProvider.getMemberId(refreshToken);
        refreshTokenStore.delete(memberId);
    }
}
