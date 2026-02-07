package monochrome.libri.global.security;

import monochrome.libri.TestFixtures;
import monochrome.libri.member.domain.Member;
import monochrome.libri.member.domain.MemberStatus;
import monochrome.libri.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private CustomUserDetailsService service;

    @Test
    void loadUserByUsername_rejectsInvalidId() {
        assertThatThrownBy(() -> service.loadUserByUsername("abc"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void loadUserByUsername_rejectsMissingMember() {
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("1"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void loadUserByUsername_rejectsInactiveMember() {
        Member member = TestFixtures.member(1L);
        member.withdraw();
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        assertThatThrownBy(() -> service.loadUserByUsername("1"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void loadUserByUsername_returnsUserPrincipal() {
        Member member = TestFixtures.member(1L);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        var user = service.loadUserByUsername("1");

        assertThat(user.getUsername()).isEqualTo("1");
        assertThat(user.getAuthorities()).hasSize(1);
    }
}
