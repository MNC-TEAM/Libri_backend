package monochrome.libri.member.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import monochrome.libri.global.domain.AuditableEntity;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(
        name = "fcm_notification_token",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_fcm_notification_token_member_token", columnNames = {"member_id", "token"})
        }
)
public class FcmNotificationToken extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fcm_notification_token_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false, length = 512)
    private String token;

    /** 마지막 사용 시각(등록·갱신 시 갱신, 오래된 토큰 정리 등에 활용) */
    @Column(name = "last_used_date", nullable = false)
    private LocalDateTime lastUsedDate;

    @PrePersist
    private void prePersistLastUsed() {
        if (lastUsedDate == null) {
            lastUsedDate = LocalDateTime.now();
        }
    }

    public void touchLastUsed() {
        this.lastUsedDate = LocalDateTime.now();
    }

    public static FcmNotificationToken create(Member member, String rawToken) {
        return FcmNotificationToken.builder()
                .member(member)
                .token(rawToken.trim())
                .lastUsedDate(LocalDateTime.now())
                .build();
    }
}
