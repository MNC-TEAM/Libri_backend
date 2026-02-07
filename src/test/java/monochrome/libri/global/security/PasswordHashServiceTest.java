package monochrome.libri.global.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordHashServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordHashService service;

    @Test
    void hashPassword_delegatesToEncoder() {
        when(passwordEncoder.encode("raw")).thenReturn("hashed");

        String result = service.hashPassword("raw");

        assertThat(result).isEqualTo("hashed");
    }

    @Test
    void matches_delegatesToEncoder() {
        when(passwordEncoder.matches("raw", "hashed")).thenReturn(true);

        boolean result = service.matches("raw", "hashed");

        assertThat(result).isTrue();
    }
}
