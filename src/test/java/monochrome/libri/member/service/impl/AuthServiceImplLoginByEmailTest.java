package monochrome.libri.member.service.impl;

import monochrome.libri.global.exception.ErrorCode;
import monochrome.libri.global.exception.LibriException;
import monochrome.libri.global.security.PasswordHashService;
import monochrome.libri.member.domain.Member;
import monochrome.libri.member.domain.MemberStatus;
import monochrome.libri.member.dto.request.EmailLoginRequestDto;
import monochrome.libri.member.dto.response.MemberResponseDto;
import monochrome.libri.member.repository.AuthRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplLoginByEmailTest {
    @Mock AuthRepository authRepository;

    @Mock
    PasswordHashService passwordHashService;


    @InjectMocks AuthServiceImpl authService; // 네 실제 구현 클래스명으로 맞춰줘

    @Test
    @DisplayName("로그인 시 회원이 없으면 INVALID_LOGIN 예외를 던진다 (비밀번호 검증 호출 X)")
    void loginByEmail_throws_whenMemberNotFound() {
        // Given
        EmailLoginRequestDto req = loginReq("  Test@Email.com  ", "password1234");
        given(authRepository.findByEmail(anyString())).willReturn(Optional.empty());

        // When
        Throwable thrown = catchThrowable(() -> authService.loginByEmail(req));

        // Then
        assertThat(thrown).isInstanceOf(LibriException.class);
        assertThat(((LibriException) thrown).getErrorCode()).isEqualTo(ErrorCode.INVALID_LOGIN);

        then(authRepository).should().findByEmail(anyString());
        then(passwordHashService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("로그인 시 비밀번호가 틀리면 INVALID_LOGIN 예외를 던진다")
    void loginByEmail_throws_whenPasswordMismatch() {
        // Given
        EmailLoginRequestDto req = loginReq("test@email.com", "wrong-password");
        Member member = memberWithHash("test@email.com", "HASH", MemberStatus.ACTIVE);

        given(authRepository.findByEmail(anyString())).willReturn(Optional.of(member));
        given(passwordHashService.matches("wrong-password", "HASH")).willReturn(false);

        // When
        Throwable thrown = catchThrowable(() -> authService.loginByEmail(req));

        // Then
        assertThat(thrown).isInstanceOf(LibriException.class);
        assertThat(((LibriException) thrown).getErrorCode()).isEqualTo(ErrorCode.INVALID_LOGIN);

        then(passwordHashService).should().matches("wrong-password", "HASH");
    }

    @Test
    @DisplayName("로그인 성공 시 비밀번호 검증 후 MemberResponseDto를 반환하며, email은 정규화(trim/lowercase)되어 조회된다")
    void loginByEmail_success_returnsMemberResponse_andUsesNormalizedEmail() {
        // Given
        EmailLoginRequestDto req = loginReq("  Test@Email.com  ", "password1234");
        Member member = memberWithHash("test@email.com", "HASH", MemberStatus.ACTIVE);

        given(authRepository.findByEmail(anyString())).willReturn(Optional.of(member));
        given(passwordHashService.matches("password1234", "HASH")).willReturn(true);

        // When
        MemberResponseDto res = authService.loginByEmail(req);

        // Then: 반환은 null 아니면 됨(필드 변동에 강하게)
        assertThat(res).isNotNull();

        // Then: 정규화된 email로 조회했는지 확인(핵심 정책)
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        then(authRepository).should().findByEmail(captor.capture());
        assertThat(captor.getValue()).isEqualTo("test@email.com");

        // Then: 비밀번호 검증이 호출됨
        then(passwordHashService).should().matches("password1234", "HASH");
    }

    /* ===== fixtures ===== */

    private EmailLoginRequestDto loginReq(String email, String rawPassword) {
        return new EmailLoginRequestDto(email, rawPassword);
    }

    private Member memberWithHash(String email, String passwordHash, MemberStatus status) {
        return Member.builder()
                .id(1L)
                .email(email)
                .passwordHash(passwordHash)
                .memberStatus(status)
                .build();
    }
}
