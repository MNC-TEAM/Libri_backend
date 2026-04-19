package monochrome.libri.notification.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import monochrome.libri.global.domain.AuditableEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "notification")
public class Notification extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;

    /** 알림을 받는 회원(노트 소유자) */
    @Column(name = "recipient_member_id", nullable = false)
    private Long recipientMemberId;

    /** 알림을 발생시킨 회원(댓글/좋아요 행위자) */
    @Column(name = "actor_member_id", nullable = false)
    private Long actorMemberId;

    /** 행위자 프로필 이미지 경로 */
    @Column(name = "actor_profile_path")
    private String actorProfilePath;

    /** 관련 노트 ID */
    @Column(name = "note_id", nullable = false)
    private Long noteId;

    @Column(nullable = false, length = 500)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 20)
    private NotificationType notificationType;

    @Builder.Default
    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    public void markAsRead() {
        this.read = true;
    }

    public void markAsDeleted() {
        this.deleted = true;
    }
}
