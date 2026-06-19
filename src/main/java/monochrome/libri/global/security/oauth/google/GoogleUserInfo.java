package monochrome.libri.global.security.oauth.google;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GoogleUserInfo(
        String id,
        String email,
        @JsonProperty("verified_email") boolean verifiedEmail,
        String name,
        String picture
) {}
