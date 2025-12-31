package monochrome.libri.global.security.token;

import java.time.Instant;

public record TokenIssueResult(
        String token,
        Instant expiresAt,
        String jti              // jwt ID
) {
}
