package monochrome.libri.global.security.oauth.google;

import com.fasterxml.jackson.annotation.JsonProperty;

record GoogleTokenInfoResponse(
        String sub,
        String email,
        @JsonProperty("email_verified") String emailVerified,
        String name,
        String picture,
        String aud
) {}
