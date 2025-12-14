package monochrome.libri.member.domain;

import jakarta.persistence.*;
import lombok.*;
import monochrome.libri.member.dto.request.MemberUpdateRequestDto;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private long id;

    @Column(length = 30)
    private SignType provider;
    private String providerUserId;

    // 로그인 수단별로 선택 필드
    @Column(length = 30)
    private String primaryEmail;
    private boolean emailVerified;

    // Oauth 인증 정보
    private String emailFromProvider;
    private Boolean emailVerifiedFromProvider;

    // 앱 내에서 사용되는 필드
    @Column(length = 30)
    private String username;
    @Column(length = 30)
    private String nickname;
    private String passwordHash;
    private String profilePath;

    // 상태 필드
    @Enumerated(EnumType.STRING)
    private Status status;
    @Enumerated(EnumType.STRING)
    private Role role;

    public Member updateMember(
            String username,
            String nickname,
            String passwordHash,
            String profilePath
    ) {
        if (username != null) {
            this.username = username;
        }
        if (nickname != null) {
            this.nickname = nickname;
        }
        if (passwordHash != null) {
            this.passwordHash = passwordHash;
        }
        if (profilePath != null) {
            this.profilePath = profilePath;
        }

        return this;
    }

    public void withdraw() {
        this.status = Status.DELETE;
    }
}
