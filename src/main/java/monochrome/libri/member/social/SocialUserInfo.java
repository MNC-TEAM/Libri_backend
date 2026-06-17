package monochrome.libri.member.social;

import monochrome.libri.member.domain.SignType;

public record SocialUserInfo(
        SignType provider,
        String providerUserId,
        String email,
        boolean emailVerified
) {
}
