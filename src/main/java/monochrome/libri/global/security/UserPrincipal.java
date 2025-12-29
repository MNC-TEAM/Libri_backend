package monochrome.libri.global.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class UserPrincipal implements UserDetails {

    /*
        * 실제 인증 주체를 나타내는 UserDetails 구현체
        * 스프링 시큐리티 내부에서는 UserDetails 타입으로 인증 주체를 다루지만,
        * 실제 애플리케이션 로직에서는 memberId를 사용하여 회원을 식별
     */
    private final Long memberId;
    private final String password;      // 형식상 필요(실제 사용 X)
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean enabled;

    public UserPrincipal(Long memberId, String password, Collection<? extends GrantedAuthority> authorities, boolean enabled) {
        this.memberId = memberId;
        this.password = password;
        this.authorities = authorities;
        this.enabled = enabled;
    }

    public Long getMemberId() {
        return memberId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    // username 자리에 memberId를 문자열로 변환해서 사용
    @Override
    public String getUsername() {
        return String.valueOf(memberId);
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return enabled; }
}
