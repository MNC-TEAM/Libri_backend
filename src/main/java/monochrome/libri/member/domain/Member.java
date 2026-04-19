package monochrome.libri.member.domain;

import jakarta.persistence.*;
import lombok.*;
import monochrome.libri.global.domain.AuditableEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(
        name = "member",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_member_email", columnNames = "email"),                                         // email unique
                @UniqueConstraint(name = "uk_member_provider_user", columnNames = {"provider", "providerUserId"})           // provider + providerUserId unique (소셜 로그인 식별자)
        }
)
public class Member extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private long id;

    // 가입/로그인 방식
    @Column(length = 30)
    @Enumerated(EnumType.STRING)
    private SignType provider;

    // 소셜 로그인
    @Column(length = 100)
    private String providerUserId;          // 소셜이면 필수, 이메일이면 null

    // Oauth 인증 정보
    private Boolean emailVerifiedFromProvider;

    // 이메일 로그인 필드
    @Column(length = 320)                   // 이메일 최대 길이 고려
    private String email;                   // 기존 primaryEmail + emailFromProvider 통합
    @Column(nullable = false)
    private boolean emailVerified;
    @Column(length = 255)
    private String passwordHash;            // 이메일 로그인만 사용, 소셜은 null

    // 앱 내에서 사용되는 필드
    @Column(length = 30)
    private String username;
    @Column(length = 30)
    private String nickname;
    private String profilePath;

    @Column(nullable = false)
    private boolean privateAccount;

    // 상태 필드
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus memberStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    public Member updateMember(
            String username,
            String nickname,
            String passwordHash,
            String profilePath,
            Boolean privateAccount
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
        if (privateAccount != null) {
            this.privateAccount = privateAccount;
        }

        return this;
    }

    public void withdraw() {
        this.memberStatus = MemberStatus.DELETE;
    }
}
