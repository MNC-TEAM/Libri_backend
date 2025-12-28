package monochrome.libri.member.service.impl;

import monochrome.libri.global.exception.ErrorCode;
import monochrome.libri.global.exception.LibriException;
import monochrome.libri.global.security.PasswordHashService;
import monochrome.libri.member.domain.Member;
import monochrome.libri.member.domain.MemberStatus;
import monochrome.libri.member.domain.Role;
import monochrome.libri.member.domain.SignType;
import monochrome.libri.member.dto.request.EmailLoginRequestDto;
import monochrome.libri.member.dto.request.EmailSignUpRequestDto;
import monochrome.libri.member.dto.response.MemberResponseDto;
import monochrome.libri.member.dto.response.SignUpResponseDto;
import monochrome.libri.member.repository.AuthRepository;
import monochrome.libri.member.service.AuthService;
import monochrome.libri.member.service.MemberService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final AuthRepository authRepository;
    private final MemberService memberService;
    private final PasswordHashService passwordHashService;

    public AuthServiceImpl(AuthRepository authRepository, MemberService memberService, PasswordHashService passwordHashService) {
        this.authRepository = authRepository;
        this.memberService = memberService;
        this.passwordHashService = passwordHashService;
    }

    /**
     * 이메일 회원가입
     */
    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private SignUpResponseDto saveOrThrowDuplicateEmail(Member member) {
        try {
            return SignUpResponseDto.from(authRepository.save(member));
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new LibriException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
    }

    @Override
    @Transactional
    //TODO: 이메일 인증 도입 시점에 emailVerified 처리 로직 추가 필요
    public SignUpResponseDto signupByEmail(EmailSignUpRequestDto request) {

        String email = normalizeEmail(request.email());

        if(authRepository.existsByEmail(email)) {
            throw new LibriException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        Member member = Member.builder()
                .provider(SignType.EMAIL)
                .providerUserId(null)                 // 이메일 가입은 소셜 식별자 없음
                .email(email)
                .emailVerified(false)                 // 이메일 인증 도입 전이면 false 고정

                .nickname(request.nickname())
                .passwordHash(passwordHashService.hashPassword(request.rawPassword()))
                .profilePath(request.profilePath())

                .memberStatus(MemberStatus.ACTIVE)
                .role(Role.USER)
                .build();

        return saveOrThrowDuplicateEmail(member);
    }

    /**
     * 이메일  로그인
     *
     */
    @Override
    public MemberResponseDto loginByEmail(EmailLoginRequestDto request) {

        String email = normalizeEmail(request.email());
        
        //회원 조회
        Member member = authRepository.findByEmail(email)
                .orElseThrow(()-> new LibriException(ErrorCode.INVALID_LOGIN));

        // 탈퇴 체크
        if(member.getMemberStatus() == MemberStatus.DELETE) {
            throw new LibriException(ErrorCode.INVALID_LOGIN);
        }

        // 소셜 계정 방어
        if (member.getPasswordHash() == null) {
            throw new LibriException(ErrorCode.INVALID_LOGIN);
        }

        // 비밀번호 체크
        boolean isPasswordMatch = passwordHashService.matches(request.rawPassword(), member.getPasswordHash());
        if(!isPasswordMatch) {
            throw new LibriException(ErrorCode.INVALID_LOGIN);
        }

        return MemberResponseDto.from(member);
    }

    @Override
    public void logout() {
        //TODO: 토큰 삭제

    }
}
