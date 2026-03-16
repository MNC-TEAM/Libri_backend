package monochrome.libri.global.security.token;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RedisRefreshTokenStore implements RefreshTokenStore {

    private static final String KEY_PREFIX = "refresh:member:";

    private final StringRedisTemplate stringRedisTemplate;

    public RedisRefreshTokenStore(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public void save(long memberId, String refreshToken, long ttlSeconds) {
        stringRedisTemplate.opsForValue().set(key(memberId), refreshToken, Duration.ofSeconds(ttlSeconds));
    }

    @Override
    public boolean matches(long memberId, String refreshToken) {
        String stored = stringRedisTemplate.opsForValue().get(key(memberId));
        return stored != null && stored.equals(refreshToken);
    }

    @Override
    public void delete(long memberId) {
        stringRedisTemplate.delete(key(memberId));
    }

    private String key(long memberId) {
        return KEY_PREFIX + memberId;
    }
}
