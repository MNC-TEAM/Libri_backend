package monochrome.libri.member.service;

import monochrome.libri.TestFixtures;
import monochrome.libri.global.exception.LibriException;
import monochrome.libri.global.security.PasswordHashService;
import monochrome.libri.member.domain.Member;
import monochrome.libri.member.domain.MemberStatus;
import monochrome.libri.member.domain.SignType;
import monochrome.libri.member.dto.request.EmailLoginRequestDto;
import monochrome.libri.member.dto.request.EmailSignUpRequestDto;
import monochrome.libri.member.dto.request.RefreshTokenRequestDto;
import monochrome.libri.member.dto.request.SocialLoginRequestDto;
import monochrome.libri.member.repository.AuthRepository;
import monochrome.libri.member.repository.SocialAccountRepository;
import monochrome.libri.member.social.SocialLoginProvider;
import monochrome.libri.member.social.SocialUserInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthRepository authRepository;

    @Mock
    private SocialAccountRepository socialAccountRepository;

    @Mock
    private PasswordHashService passwordHashService;

    @Mock
    private SocialLoginProvider kakaoSocialLoginProvider;

    private monochrome.libri.member.service.impl.AuthServiceImpl service;

    @BeforeEach
    void setUp() {
        when(kakaoSocialLoginProvider.provider()).thenReturn(SignType.KAKAO);
        service = new monochrome.libri.member.service.impl.AuthServiceImpl(
                authRepository,
                socialAccountRepository,
                passwordHashService,
                List.of(kakaoSocialLoginProvider)
        );
    }

    @Test
    void signupByEmail_rejectsDuplicate() {
        when(authRepository.existsByEmail("user@test.com")).thenReturn(true);

        EmailSignUpRequestDto dto = new EmailSignUpRequestDto("user@test.com", "password", "nick", null);

        assertThatThrownBy(() -> service.signupByEmail(dto))
                .isInstanceOf(LibriException.class);
    }

    @Test
    void signupByEmail_savesNormalizedEmail() {
        when(authRepository.existsByEmail("user@test.com")).thenReturn(false);
        when(passwordHashService.hashPassword("password")).thenReturn("hashed");

        EmailSignUpRequestDto dto = new EmailSignUpRequestDto(" USER@TEST.COM ", "password", "nick", null);

        service.signupByEmail(dto);

        ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
        verify(authRepository).save(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo("user@test.com");
        assertThat(captor.getValue().getPasswordHash()).isEqualTo("hashed");
    }

    @Test
    void loginByEmail_rejectsWrongPassword() {
        Member member = TestFixtures.member(1L);
        when(authRepository.findByEmail("user@test.com")).thenReturn(Optional.of(member));
        when(passwordHashService.matches("wrong", member.getPasswordHash())).thenReturn(false);

        EmailLoginRequestDto dto = new EmailLoginRequestDto("user@test.com", "wrong");

        assertThatThrownBy(() -> service.loginByEmail(dto))
                .isInstanceOf(LibriException.class);
    }

    @Test
    void loginByEmail_rejectsDeletedMember() {
        Member member = TestFixtures.member(1L);
        member.withdraw();
        when(authRepository.findByEmail("user@test.com")).thenReturn(Optional.of(member));

        EmailLoginRequestDto dto = new EmailLoginRequestDto("user@test.com", "pass");

        assertThatThrownBy(() -> service.loginByEmail(dto))
                .isInstanceOf(LibriException.class);
    }

    @Test
    void loginByEmail_returnsMemberResponse() {
        Member member = TestFixtures.member(1L);
        when(authRepository.findByEmail("user@test.com")).thenReturn(Optional.of(member));
        when(passwordHashService.matches("pass", member.getPasswordHash())).thenReturn(true);

        EmailLoginRequestDto dto = new EmailLoginRequestDto("user@test.com", "pass");
        var response = service.loginByEmail(dto);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.nickname()).isEqualTo(member.getNickname());
    }

    @Test
    void loginBySocial_linksExistingEmailMember() {
        Member member = TestFixtures.member(1L);
        SocialUserInfo userInfo = new SocialUserInfo(SignType.KAKAO, "kakao-1", "user1@test.com", true);

        when(kakaoSocialLoginProvider.authenticate(any())).thenReturn(userInfo);
        when(socialAccountRepository.findByProviderAndProviderUserId(SignType.KAKAO, "kakao-1"))
                .thenReturn(Optional.empty());
        when(authRepository.findByEmail("user1@test.com")).thenReturn(Optional.of(member));
        when(socialAccountRepository.findByMemberIdAndProvider(1L, SignType.KAKAO))
                .thenReturn(Optional.empty());

        var response = service.loginBySocial(new SocialLoginRequestDto(SignType.KAKAO, null, "code"));

        assertThat(response.id()).isEqualTo(1L);
        verify(socialAccountRepository).save(any());
    }

    @Test
    void loginBySocial_returnsLinkedMember() {
        Member member = TestFixtures.member(1L);
        SocialUserInfo userInfo = new SocialUserInfo(SignType.KAKAO, "kakao-1", "user1@test.com", true);

        when(kakaoSocialLoginProvider.authenticate(any())).thenReturn(userInfo);
        when(socialAccountRepository.findByProviderAndProviderUserId(SignType.KAKAO, "kakao-1"))
                .thenReturn(Optional.of(monochrome.libri.member.domain.SocialAccount.builder()
                        .id(1L)
                        .member(member)
                        .provider(SignType.KAKAO)
                        .providerUserId("kakao-1")
                        .emailVerifiedFromProvider(true)
                        .build()));

        var response = service.loginBySocial(new SocialLoginRequestDto(SignType.KAKAO, null, "code"));

        assertThat(response.id()).isEqualTo(1L);
        verify(socialAccountRepository, never()).save(any());
        verify(authRepository, never()).save(any());
    }

    @Test
    void loginBySocial_createsMemberWhenNoEmailMatch() {
        SocialUserInfo userInfo = new SocialUserInfo(SignType.KAKAO, "kakao-2", "new@test.com", true);
        Member savedMember = TestFixtures.member(2L);

        when(kakaoSocialLoginProvider.authenticate(any())).thenReturn(userInfo);
        when(socialAccountRepository.findByProviderAndProviderUserId(SignType.KAKAO, "kakao-2"))
                .thenReturn(Optional.empty());
        when(authRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(authRepository.save(any(Member.class))).thenReturn(savedMember);
        when(socialAccountRepository.findByMemberIdAndProvider(2L, SignType.KAKAO))
                .thenReturn(Optional.empty());

        var response = service.loginBySocial(new SocialLoginRequestDto(SignType.KAKAO, null, "code"));

        assertThat(response.id()).isEqualTo(2L);
        verify(authRepository).save(any(Member.class));
        verify(socialAccountRepository).save(any());
    }

    @Test
    void validateRefreshRequest_rejectsBlankToken() {
        RefreshTokenRequestDto dto = new RefreshTokenRequestDto(" ");

        assertThatThrownBy(() -> service.validateRefreshRequest(dto))
                .isInstanceOf(LibriException.class);
    }

    @Test
    void logout_rejectsBlankToken() {
        assertThatThrownBy(() -> service.logout(" "))
                .isInstanceOf(LibriException.class);
    }
}
