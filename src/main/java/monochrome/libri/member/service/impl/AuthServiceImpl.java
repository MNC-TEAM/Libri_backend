package monochrome.libri.member.service.impl;

import monochrome.libri.global.exception.ErrorCode;
import monochrome.libri.global.exception.LibriException;
import monochrome.libri.global.security.PasswordHashService;
import monochrome.libri.member.domain.Member;
import monochrome.libri.member.domain.Role;
import monochrome.libri.member.domain.SignType;
import monochrome.libri.member.domain.Status;
import monochrome.libri.member.dto.request.EmailLoginRequestDto;
import monochrome.libri.member.dto.request.MemberCreateRequestDto;
import monochrome.libri.member.dto.response.MemberResponseDto;
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
    @Override
    @Transactional
    public MemberResponseDto signupByEmail(MemberCreateRequestDto request) {

        if(authRepository.existsByPrimaryEmail(request.primaryEmail())) {
            throw new LibriException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        Member member = Member.builder()
                .provider(SignType.EMAIL)
                .providerUserId(request.providerUserId())
                .primaryEmail(request.primaryEmail())
                .emailVerified(false)
                .emailFromProvider(null)
                .emailVerifiedFromProvider(null)
                .username(request.username())
                .nickname(request.nickname())
                .passwordHash(passwordHashService.hashPassword(request.rawPassword()))
                .profilePath(null)
                .status(Status.ACTIVE)
                .role(Role.USER)
                .build();

        Member saved = authRepository.save(member);

        return MemberResponseDto.from(saved);
    }

    /**
     * 이메일  로그인
     *
     */
    @Override
    public MemberResponseDto loginByEmail(EmailLoginRequestDto request) {
        
        //회원 조회
        Member member = authRepository.findByPrimaryEmail(request.email())
                .orElseThrow(()-> new LibriException(ErrorCode.INVALID_LOGIN));

        // 탈퇴 체크
        if(member.getStatus() == Status.DELETE) {
            throw new LibriException(ErrorCode.MEMBER_WITHDRAWN);
        }

        // 비밀번호 체크
        if(!passwordHashService.matches(request.password(), member.getPasswordHash())) {
            throw new LibriException(ErrorCode.INVALID_LOGIN);
        }

        //TODO: 토큰이 정해지면 그 토큰 저장(아마 JWT를 쓰되 Refresh를 길게 가져갈듯)

        return MemberResponseDto.from(member);
    }

    @Override
    public void logout() {
        //TODO: 토큰 삭제

    }
}
