package monochrome.libri.global.security;

import monochrome.libri.member.domain.Member;
import monochrome.libri.member.domain.MemberStatus;
import monochrome.libri.member.repository.MemberRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    public CustomUserDetailsService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String memberIdStr) throws UsernameNotFoundException {
        long memberId;

        // username을 memberId로 변환
        try {
            memberId = Long.parseLong(memberIdStr);
        } catch (NumberFormatException e) {
            throw new UsernameNotFoundException("Invalid member ID: " + memberIdStr);
        }

        // 회원 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new UsernameNotFoundException("Member not found with ID: " + memberId));

        // 상태 체크(ACTIVE만 허용)
        if (member.getMemberStatus() != MemberStatus.ACTIVE) {
            throw new UsernameNotFoundException("Member is not active with ID: " + memberId);
        }

        // 권한 매핑
        String roleName = member.getRole().name();
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + roleName));

        return new UserPrincipal(
                member.getId(),
                null,       // 비밀번호는 사용하지 않음
                authorities,
                true                // 활성화된 계정
        );
    }
}
