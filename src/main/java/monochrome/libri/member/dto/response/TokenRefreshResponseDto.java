package monochrome.libri.member.dto.response;

import monochrome.libri.global.security.token.TokenPair;

public record TokenRefreshResponseDto(
        String tokenType,
        String accessToken,
        String refreshToken
) {
    public static TokenRefreshResponseDto from(TokenPair tokenPair) {
        return new TokenRefreshResponseDto(
                tokenPair.tokenType(),
                tokenPair.accessToken(),
                tokenPair.refreshToken()
        );
    }
}
