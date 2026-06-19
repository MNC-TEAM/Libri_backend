package monochrome.libri.member.service.impl;

import monochrome.libri.global.exception.ErrorCode;
import monochrome.libri.global.exception.LibriException;
import monochrome.libri.global.security.PasswordHashService;
import monochrome.libri.member.domain.Member;
import monochrome.libri.member.domain.MemberStatus;
import monochrome.libri.member.domain.Role;
import monochrome.libri.member.domain.SignType;
import monochrome.libri.member.domain.SocialAccount;
import monochrome.libri.member.dto.request.EmailLoginRequestDto;
import monochrome.libri.member.dto.request.EmailSignUpRequestDto;
import monochrome.libri.member.dto.request.RefreshTokenRequestDto;
import monochrome.libri.member.dto.request.SocialLoginRequestDto;
import monochrome.libri.member.dto.response.MemberResponseDto;
import monochrome.libri.member.repository.AuthRepository;
import monochrome.libri.member.repository.SocialAccountRepository;
import monochrome.libri.member.service.AuthService;
import monochrome.libri.member.social.SocialLoginProvider;
import monochrome.libri.member.social.SocialUserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final AuthRepository authRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final PasswordHashService passwordHashService;
    private final Map<SignType, SocialLoginProvider> socialLoginProviders;

    public AuthServiceImpl(
            AuthRepository authRepository,
            SocialAccountRepository socialAccountRepository,
            PasswordHashService passwordHashService,
            List<SocialLoginProvider> socialLoginProviders
    ) {
        this.authRepository = authRepository;
        this.socialAccountRepository = socialAccountRepository;
        this.passwordHashService = passwordHashService;
        this.socialLoginProviders = toProviderMap(socialLoginProviders);
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private String maskEmail(String email) {
        if (email == null || email.isBlank()) return "-";
        int at = email.indexOf('@');
        if (at <= 1) return "***";
        String local = email.substring(0, at);
        String domain = email.substring(at);
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

        if (authRepository.existsByEmail(email)) {
            log.warn("auth.signup result=fail reason=duplicate_email email={}", maskedEmail);
            throw new LibriException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        Member member = Member.builder()
                .provider(SignType.EMAIL)
                .providerUserId(null)
                .email(email)
                .emailVerified(false)
                .nickname(request.nickname())
                .passwordHash(passwordHashService.hashPassword(request.rawPassword()))
                .profilePath(request.profilePath())
                .memberStatus(MemberStatus.ACTIVE)
                .role(Role.USER)
                .build();

        log.info("auth.signup result=success memberId={} email={}", member.getId(), maskedEmail);
        saveOrThrowDuplicateEmail(member);
    }

    @Override
    public MemberResponseDto loginByEmail(EmailLoginRequestDto request) {
        String email = normalizeEmail(request.email());
        String maskedEmail = maskEmail(email);

        Member member = authRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("auth.login result=fail reason=invalid_login email={}", maskedEmail);
                    return new LibriException(ErrorCode.INVALID_LOGIN);
                });

        if (member.getMemberStatus() == MemberStatus.DELETE) {
            throw new LibriException(ErrorCode.INVALID_LOGIN);
        }
        if (member.getPasswordHash() == null) {
            throw new LibriException(ErrorCode.INVALID_LOGIN);
        }
        if (!passwordHashService.matches(request.rawPassword(), member.getPasswordHash())) {
            throw new LibriException(ErrorCode.INVALID_LOGIN);
        }

        log.info("auth.login result=success memberId={}", member.getId());
        return MemberResponseDto.from(member);
    }

    @Override
    @Transactional
    public MemberResponseDto loginBySocial(SocialLoginRequestDto request) {
        SignType provider = request.provider();
        if (provider == null || provider == SignType.EMAIL) {
            throw new LibriException(ErrorCode.INVALID_INPUT_VALUE);
        }

        SocialLoginProvider socialLoginProvider = socialLoginProviders.get(provider);
        if (socialLoginProvider == null) {
            throw new LibriException(ErrorCode.INVALID_INPUT_VALUE);
        }

        SocialUserInfo userInfo = socialLoginProvider.authenticate(request);

        Member member = socialAccountRepository
                .findByProviderAndProviderUserId(userInfo.provider(), userInfo.providerUserId())
                .map(SocialAccount::getMember)
                .orElseGet(() -> findOrCreateMemberForSocialLogin(userInfo));

        if (member.getMemberStatus() == MemberStatus.DELETE) {
            throw new LibriException(ErrorCode.INVALID_LOGIN);
        }

        member.verifyEmailFromSocialProvider(userInfo.emailVerified());
        log.info("auth.social-login result=success memberId={} provider={}", member.getId(), userInfo.provider());
        return MemberResponseDto.from(member);
    }

    @Override
    public void logout(String refreshToken) {
        validateRefreshTokenText(refreshToken);
    }

    @Override
    public void validateRefreshRequest(RefreshTokenRequestDto request) {
        if (request == null) {
            throw new LibriException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        validateRefreshTokenText(request.refreshToken());
    }

    private void validateRefreshTokenText(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new LibriException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
    }

    private Map<SignType, SocialLoginProvider> toProviderMap(List<SocialLoginProvider> providers) {
        Map<SignType, SocialLoginProvider> providerMap = new EnumMap<>(SignType.class);
        for (SocialLoginProvider provider : providers) {
            providerMap.put(provider.provider(), provider);
        }
        return providerMap;
    }

    private Member findOrCreateMemberForSocialLogin(SocialUserInfo userInfo) {
        if (userInfo.email() != null) {
            Member existingMember = authRepository.findByEmail(normalizeEmail(userInfo.email()))
                    .orElse(null);
            if (existingMember != null) {
                ensureMemberCanLinkProvider(existingMember, userInfo);
                return linkSocialAccount(existingMember, userInfo);
            }
        }

        Member newMember = createSocialMember(userInfo);
        Member savedMember = saveSocialMember(newMember);
        return linkSocialAccount(savedMember, userInfo);
    }

    private void ensureMemberCanLinkProvider(Member member, SocialUserInfo userInfo) {
        if (member.getMemberStatus() == MemberStatus.DELETE) {
            throw new LibriException(ErrorCode.INVALID_LOGIN);
        }
        socialAccountRepository.findByMemberIdAndProvider(member.getId(), userInfo.provider())
                .ifPresent(existing -> {
                    if (!existing.getProviderUserId().equals(userInfo.providerUserId())) {
                        throw new LibriException(ErrorCode.AUTHENTICATION_FAILED);
                    }
                });
    }

    private Member linkSocialAccount(Member member, SocialUserInfo userInfo) {
        socialAccountRepository.findByProviderAndProviderUserId(userInfo.provider(), userInfo.providerUserId())
                .ifPresent(existing -> {
                    if (existing.getMember().getId() != member.getId()) {
                        throw new LibriException(ErrorCode.AUTHENTICATION_FAILED);
                    }
                });

        if (socialAccountRepository.findByMemberIdAndProvider(member.getId(), userInfo.provider()).isEmpty()) {
            try {
                socialAccountRepository.save(SocialAccount.builder()
                        .member(member)
                        .provider(userInfo.provider())
                        .providerUserId(userInfo.providerUserId())
                        .emailVerifiedFromProvider(userInfo.emailVerified())
                        .build());
            } catch (org.springframework.dao.DataIntegrityViolationException e) {
                throw new LibriException("소셜 계정 연동 저장에 실패했습니다.", e, ErrorCode.AUTHENTICATION_FAILED);
            }
        }

        member.verifyEmailFromSocialProvider(userInfo.emailVerified());
        return member;
    }

    private Member createSocialMember(SocialUserInfo userInfo) {
        String email = normalizeEmail(userInfo.email());
        String identitySuffix = userInfo.providerUserId().replaceAll("[^A-Za-z0-9]", "");
        if (identitySuffix.isBlank()) {
            identitySuffix = "user";
        }
        String base = userInfo.provider().name().toLowerCase() + "_" + identitySuffix;
        String username = truncate(base, 30);
        String nickname = truncate(base, 30);

        return Member.builder()
                .provider(userInfo.provider())
                .providerUserId(userInfo.providerUserId())
                .email(email)
                .emailVerified(userInfo.emailVerified())
                .emailVerifiedFromProvider(userInfo.emailVerified())
                .username(username)
                .nickname(nickname)
                .privateAccount(false)
                .memberStatus(MemberStatus.ACTIVE)
                .role(Role.USER)
                .build();
    }

    private Member saveSocialMember(Member member) {
        try {
            return authRepository.save(member);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            if (member.getEmail() != null) {
                Member existingMember = authRepository.findByEmail(member.getEmail()).orElse(null);
                if (existingMember != null) {
                    return existingMember;
                }
            }
            throw new LibriException("소셜 회원 생성에 실패했습니다.", e, ErrorCode.AUTHENTICATION_FAILED);
        }
    }

    private String truncate(String value, int maxLength) {
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
