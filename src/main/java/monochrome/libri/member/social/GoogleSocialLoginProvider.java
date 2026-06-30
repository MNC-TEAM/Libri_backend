package monochrome.libri.member.social;

import monochrome.libri.global.exception.ErrorCode;
import monochrome.libri.global.exception.LibriException;
import monochrome.libri.global.security.oauth.google.GoogleOAuthClient;
import monochrome.libri.global.security.oauth.google.GoogleUserInfo;
import monochrome.libri.member.domain.SignType;
import monochrome.libri.member.dto.request.SocialLoginRequestDto;
import org.springframework.stereotype.Component;

@Component
public class GoogleSocialLoginProvider implements SocialLoginProvider {

    private final GoogleOAuthClient googleOAuthClient;

    public GoogleSocialLoginProvider(GoogleOAuthClient googleOAuthClient) {
        this.googleOAuthClient = googleOAuthClient;
    }

    @Override
    public SignType provider() {
        return SignType.GOOGLE;
    }

    @Override
    public SocialUserInfo authenticate(SocialLoginRequestDto request) {
        String idToken = request.idToken();
        if (idToken == null || idToken.isBlank()) {
            throw new LibriException(ErrorCode.INVALID_INPUT_VALUE);
        }

        GoogleUserInfo userInfo = googleOAuthClient.verifyIdToken(idToken);
        return new SocialUserInfo(
                SignType.GOOGLE,
                userInfo.id(),
                userInfo.email(),
                userInfo.verifiedEmail()
        );
    }
}
