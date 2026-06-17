package monochrome.libri.member.social;

import monochrome.libri.member.domain.SignType;
import monochrome.libri.member.dto.request.SocialLoginRequestDto;

public interface SocialLoginProvider {
    SignType provider();

    SocialUserInfo authenticate(SocialLoginRequestDto request);
}
