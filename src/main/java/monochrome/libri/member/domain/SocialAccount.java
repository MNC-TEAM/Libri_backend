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
        name = "social_account",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_social_account_provider_user", columnNames = {"provider", "providerUserId"}),
                @UniqueConstraint(name = "uk_social_account_member_provider", columnNames = {"member_id", "provider"})
        }
)
public class SocialAccount extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "social_account_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SignType provider;

    @Column(nullable = false, length = 100)
    private String providerUserId;

    @Column(nullable = false)
    private boolean emailVerifiedFromProvider;
}
