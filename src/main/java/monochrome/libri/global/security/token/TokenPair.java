package monochrome.libri.global.security.token;

public record TokenPair(
        String tokenType,
        String accessToken,
        String refreshToken
) {
    public static TokenPair bearer(String accessToken, String refreshToken) {
        return new TokenPair("Bearer", accessToken, refreshToken);
    }
}
