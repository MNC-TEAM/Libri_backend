package monochrome.libri.member.service;

import monochrome.libri.member.dto.request.EmailLoginRequestDto;
import monochrome.libri.member.dto.request.MemberCreateRequestDto;
import monochrome.libri.member.dto.response.MemberResponseDto;

/**
 * 인증(로그인, 로그아웃, 회원가입, 탈퇴) 흐름을 담당하는 서비스
 * - 이메일 회원가입, 로그인
 */
public interface AuthService {
    /**
     * 이메일 기반 회원가입
     * @param   request 회원가입 회원 저보
     * @return  생성된 회원 정보
     */
    MemberResponseDto signupByEmail(MemberCreateRequestDto request);

    /**
     * 이메일 로그인
     * @param request 로그인 요청 정보(email, password)
     * @return 로그인한 회원 정보
     */
    MemberResponseDto loginByEmail(EmailLoginRequestDto request);

    /**
     * 로그아웃
     */
    void logout();
}
