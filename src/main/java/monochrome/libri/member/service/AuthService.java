package monochrome.libri.member.service;

import monochrome.libri.member.dto.request.EmailLoginRequestDto;
import monochrome.libri.member.dto.request.EmailSignUpRequestDto;
import monochrome.libri.member.dto.request.RefreshTokenRequestDto;
import monochrome.libri.member.dto.request.SocialLoginRequestDto;
import monochrome.libri.member.dto.response.MemberResponseDto;

public interface AuthService {
    void signupByEmail(EmailSignUpRequestDto request);

    MemberResponseDto loginByEmail(EmailLoginRequestDto request);

    MemberResponseDto loginBySocial(SocialLoginRequestDto request);

    void logout(String refreshToken);

    void validateRefreshRequest(RefreshTokenRequestDto request);
}
