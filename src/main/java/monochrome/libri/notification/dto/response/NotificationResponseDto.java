package monochrome.libri.notification.dto.response;

import monochrome.libri.notification.domain.NotificationType;

import java.time.LocalDateTime;

public record NotificationResponseDto(
        Long id,
        Long noteId,
        String content,
        String actorProfilePath,
        Long actorMemberId,
        boolean read,
        LocalDateTime createdAt,
        NotificationType notificationType
) {
}
