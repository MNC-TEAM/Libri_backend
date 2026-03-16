package monochrome.libri.global.security.token;

public interface RefreshTokenStore {
    void save(long memberId, String refreshToken, long ttlSeconds);

    boolean matches(long memberId, String refreshToken);

    void delete(long memberId);
}
