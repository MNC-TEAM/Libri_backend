package monochrome.libri.member.service;

import monochrome.libri.TestFixtures;
import monochrome.libri.global.exception.LibriException;
import monochrome.libri.global.security.PasswordHashService;
import monochrome.libri.member.domain.Member;
import monochrome.libri.member.domain.MemberStatus;
import monochrome.libri.member.dto.request.EmailLoginRequestDto;
import monochrome.libri.member.dto.request.EmailSignUpRequestDto;
import monochrome.libri.member.dto.request.RefreshTokenRequestDto;
import monochrome.libri.member.repository.AuthRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    private MemberService memberService;

    @Mock
    private PasswordHashService passwordHashService;

    @InjectMocks
    private monochrome.libri.member.service.impl.AuthServiceImpl service;

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
