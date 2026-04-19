package monochrome.libri.notification.service.impl;

import monochrome.libri.firebase.FcmPushSender;
import monochrome.libri.notification.domain.Notification;
import monochrome.libri.notification.domain.NotificationType;
import monochrome.libri.notification.dto.response.NotificationResponseDto;
import monochrome.libri.notification.dto.response.NotificationSliceResponseDto;
import monochrome.libri.notification.repository.NotificationRepository;
import monochrome.libri.notification.service.NotificationService;
import monochrome.libri.global.exception.ErrorCode;
import monochrome.libri.global.exception.LibriException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final NotificationRepository notificationRepository;
    private final FcmPushSender fcmPushSender;

    public NotificationServiceImpl(NotificationRepository notificationRepository, FcmPushSender fcmPushSender) {
        this.notificationRepository = notificationRepository;
        this.fcmPushSender = fcmPushSender;
    }

    @Override
    @Transactional
    public void createNotification(
            long recipientMemberId,
            long actorMemberId,
            String actorProfilePath,
            long noteId,
            String content,
            NotificationType notificationType
    ) {
        if (recipientMemberId == actorMemberId) {
            return;
        }
        Notification notification = Notification.builder()
                .recipientMemberId(recipientMemberId)
                .actorMemberId(actorMemberId)
                .actorProfilePath(actorProfilePath != null ? actorProfilePath : "")
                .noteId(noteId)
                .content(content)
                .notificationType(notificationType)
                .build();
        Notification saved = notificationRepository.save(notification);
        try {
            fcmPushSender.sendToMember(
                    recipientMemberId,
                    "Libri",
                    content,
                    Map.of(
                            "notificationId", String.valueOf(saved.getId()),
                            "noteId", String.valueOf(noteId),
                            "type", notificationType.name()
                    )
            );
        } catch (Exception e) {
            log.warn("FCM 푸시 후처리 중 오류 (알림 저장은 완료됨) notificationId={}", saved.getId(), e);
        }
    }

    @Override
    public NotificationSliceResponseDto getNotifications(long memberId, Pageable pageable) {
        Slice<Notification> slice = notificationRepository
                .findByRecipientMemberIdAndDeletedFalseOrderByCreatedDateDesc(memberId, pageable);
        List<NotificationResponseDto> content = slice.getContent().stream()
                .map(n -> new NotificationResponseDto(
                        n.getId(),
                        n.getNoteId(),
                        n.getContent(),
                        n.getActorProfilePath(),
                        n.getActorMemberId(),
                        n.isRead(),
                        n.getCreatedDate(),
                        n.getNotificationType()
                ))
                .toList();
        long totalCount = notificationRepository.countByRecipientMemberIdAndDeletedFalse(memberId);
        return new NotificationSliceResponseDto(
                totalCount,
                content,
                slice.hasNext(),
                slice.getNumber(),
                slice.getSize()
        );
    }

    @Override
    @Transactional
    public void readNotification(long notificationId, long memberId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new LibriException(ErrorCode.NOTIFICATION_NOT_FOUND));
        if (notification.getRecipientMemberId() != memberId) {
            throw new LibriException(ErrorCode.ACCESS_DENIED);
        }
        notification.markAsRead();
    }

    @Override
    @Transactional
    public void readAllNotifications(long memberId) {
        List<Notification> notifications = notificationRepository
                .findByRecipientMemberIdAndReadFalseAndDeletedFalse(memberId);
        notifications.forEach(Notification::markAsRead);
    }

    @Override
    @Transactional
    public void deleteNotification(long notificationId, long memberId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new LibriException(ErrorCode.NOTIFICATION_NOT_FOUND));
        if (notification.getRecipientMemberId() != memberId) {
            throw new LibriException(ErrorCode.ACCESS_DENIED);
        }
        notification.markAsDeleted();
    }

    @Override
    @Transactional
    public void deleteAllNotifications(long memberId) {
        List<Notification> notifications = notificationRepository.findByRecipientMemberIdAndDeletedFalse(memberId);
        notifications.forEach(Notification::markAsDeleted);
    }
}
