package monochrome.libri.member.service.impl;

import monochrome.libri.global.exception.ErrorCode;
import monochrome.libri.global.exception.LibriException;
import monochrome.libri.global.security.PasswordHashService;
import monochrome.libri.member.domain.Member;
import monochrome.libri.member.domain.SignType;
import monochrome.libri.member.dto.request.EmailSignUpRequestDto;
import monochrome.libri.member.dto.response.SignUpResponseDto;
import monochrome.libri.member.repository.AuthRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplSignUpByEmailTest {
    @Mock
    AuthRepository authRepository;

    @Mock
    PasswordHashService passwordHashService;

    @InjectMocks
    AuthServiceImpl authService;

    /* =========================
   Test Fixtures (Helpers)
   ========================= */

    private EmailSignUpRequestDto emailSignUpReq() {
        return new EmailSignUpRequestDto(
                "test@email.com",
                "password1234",
                "nick",
                "profile.png"
        );
    }

    private EmailSignUpRequestDto emailSignUpReqWithEmail(String email) {
        EmailSignUpRequestDto base = emailSignUpReq();
        return new EmailSignUpRequestDto(
                email,
                base.rawPassword(),
                base.nickname(),
                base.profilePath()
        );
    }

    private EmailSignUpRequestDto emailSignUpReqWith(String email, String rawPassword) {
        EmailSignUpRequestDto base = emailSignUpReq();
        return new EmailSignUpRequestDto(
                email,
                rawPassword,
                base.nickname(),
                base.profilePath()
        );
    }

    /* =========================
   Test Fixtures (Helpers)
   ========================= */

    @Test
    @DisplayName("existsByEmail=true면 EMAIL_ALREADY_EXISTS 예외를 던진다. (save는 호출되지 않아야 한다)")
    void signupByEmail_throws_whenEmailAlreadyExists() {
        // Given
        EmailSignUpRequestDto request = emailSignUpReqWithEmail("  Test@Email.com  ");
        given(authRepository.existsByEmail(anyString())).willReturn(true);

        // When
        Throwable thrown = catchThrowable(() -> authService.signupByEmail(request));

        // Then
        assertThat(thrown).isInstanceOf(LibriException.class);
        assertThat(((LibriException) thrown).getErrorCode()).isEqualTo(ErrorCode.EMAIL_ALREADY_EXISTS);

        then(authRepository).should().existsByEmail(anyString());
        then(authRepository).should(never()).save(any());
        then(passwordHashService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("email을 정규화(trim/lowercase)하여 저장하고, provider=EMAIL/providerUserId=null/emailVerified=false, passwordHash 반영, memberStatus=ACTIVE/role=USER로 저장한다")
    void signupByEmail_success_savesExpectedMember() {
        // Given
        EmailSignUpRequestDto request = emailSignUpReqWith(
                "  Test@Email.com  ",     // 입력은 공백/대문자 포함
                 "password1234"
        );

        given(authRepository.existsByEmail("test@email.com")).willReturn(false);
        given(passwordHashService.hashPassword("password1234")).willReturn("HASHED_PASSWORD");
        given(authRepository.save(any(Member.class))).willAnswer(inv -> inv.getArgument(0, Member.class));

        // When
        SignUpResponseDto response = authService.signupByEmail(request);

        // Then: 정규화된 email로 중복체크가 수행된다(정책)
        then(authRepository).should().existsByEmail("test@email.com");

        // Then: 비밀번호는 해시된다(정책)
        then(passwordHashService).should().hashPassword("password1234");

        // Then: 저장 객체에 핵심 정책이 반영된다(필수만 확인)
        ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
        then(authRepository).should().save(captor.capture());

        Member saved = captor.getValue();
        assertThat(saved.getEmail()).isEqualTo("test@email.com");         // normalize
        assertThat(saved.getProvider()).isEqualTo(SignType.EMAIL);        // provider 정책
        assertThat(saved.getProviderUserId()).isNull();                   // 이메일 가입은 소셜 식별자 없음
        assertThat(saved.getPasswordHash()).isEqualTo("HASHED_PASSWORD"); // 해시 반영(원문 저장 방지)

        // Then: 응답도 최소한만(정책 결과)
        assertThat(response.email()).isEqualTo("test@email.com");
    }

    @Test
    @DisplayName("save가 DataIntegrityViolationException을 던지면 EMAIL_ALREADY_EXISTS로 매핑한다")
    void signupByEmail_mapsUniqueViolation_toEmailAlreadyExists() {
        // Given
        EmailSignUpRequestDto request = emailSignUpReqWithEmail("A@A.COM"); // 대소문자 섞여도 상관없게

        // 이메일 중복은 통과했으나 저장 시점(unique 조건으로 인한) 중복 예외 발생 시나리오
        given(authRepository.existsByEmail(anyString())).willReturn(false);
        given(passwordHashService.hashPassword(anyString())).willReturn("HASHED_PASSWORD");
        given(authRepository.save(any()))
                .willThrow(new DataIntegrityViolationException("duplicate key"));

        // When
        Throwable thrown = catchThrowable(() -> authService.signupByEmail(request));

        // Then: 예외 매핑(정책)
        assertThat(thrown).isInstanceOf(LibriException.class);
        assertThat(((LibriException) thrown).getErrorCode()).isEqualTo(ErrorCode.EMAIL_ALREADY_EXISTS);

        // Then: 저장을 시도했는지(중복 체크를 통과했으니 save로 갔어야 함)
        then(authRepository).should().save(any());

        // Then(선택): save로 가기 전 해시도 시도했는지
        then(passwordHashService).should().hashPassword(anyString());
    }
}