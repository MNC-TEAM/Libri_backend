package monochrome.libri.member.service.impl;

import lombok.extern.slf4j.Slf4j;
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
import monochrome.libri.member.repository.AuthRepository;
import monochrome.libri.member.service.AuthService;
import monochrome.libri.member.service.MemberService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
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

    // 최소 마스킹(로깅용)
    private String maskEmail(String email) {
        if (email == null || email.isBlank()) return "-";
        int at = email.indexOf('@');
        if (at <= 1) return "***";
        String local = email.substring(0, at);
        String domain = email.substring(at); // includes '@'
        return local.charAt(0) + "***" + local.charAt(local.length() - 1) + domain;
    }

    private void saveOrThrowDuplicateEmail(Member member) {
        try {
            authRepository.save(member);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new LibriException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
    }

    @Override
    @Transactional
    //TODO: 이메일 인증 도입 시점에 emailVerified 처리 로직 추가 필요
    public void signupByEmail(EmailSignUpRequestDto request) {

        String email = normalizeEmail(request.email());
        String maskedEmail = maskEmail(email);

        if(authRepository.existsByEmail(email)) {
            log.warn("auth.signup result=fail reason=duplicate_email email={}", maskedEmail);
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

        log.info("auth.signup result=success memberId={} email={}",member.getId(), maskedEmail);

        saveOrThrowDuplicateEmail(member);
    }

    /**
     * 이메일  로그인
     *
     */
    @Override
    public MemberResponseDto loginByEmail(EmailLoginRequestDto request) {

        String email = normalizeEmail(request.email());
        String maskedEmail = maskEmail(email);
        
        //회원 조회
        Member member = authRepository.findByEmail(email)
                .orElseThrow(()-> {
                            log.warn("auth.login result=fail reason=invalid_login email={}", maskedEmail);
                            return new LibriException(ErrorCode.INVALID_LOGIN);
                        }
                );

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

        log.info("auth.login result=success memberId={}", member.getId());

        return MemberResponseDto.from(member);
    }

    @Override
    public void logout() {
        //TODO: 토큰 삭제

    }
}
