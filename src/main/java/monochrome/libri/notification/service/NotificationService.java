package monochrome.libri.notification.service;

import monochrome.libri.notification.domain.NotificationType;
import monochrome.libri.notification.dto.response.NotificationSliceResponseDto;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    /**
     * 알림 생성 (노트 소유자에게만, 본인 제외)
     */
    void createNotification(
            long recipientMemberId,
            long actorMemberId,
            String actorProfilePath,
            long noteId,
            String content,
            NotificationType notificationType
    );

    /**
     * 알림 생성 — noteId 없는 경우 (FOLLOW 등)
     */
    void createNotification(
            long recipientMemberId,
            long actorMemberId,
            String actorProfilePath,
            String content,
            NotificationType notificationType
    );

    NotificationSliceResponseDto getNotifications(long memberId, Pageable pageable);

    void readNotification(long notificationId, long memberId);

    void readAllNotifications(long memberId);

    void deleteNotification(long notificationId, long memberId);

    void deleteAllNotifications(long memberId);
}
