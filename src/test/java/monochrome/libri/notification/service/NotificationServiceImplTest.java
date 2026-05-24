package monochrome.libri.notification.service;

import monochrome.libri.firebase.FcmPushSender;
import monochrome.libri.global.exception.LibriException;
import monochrome.libri.notification.domain.Notification;
import monochrome.libri.notification.domain.NotificationType;
import monochrome.libri.notification.dto.response.NotificationSliceResponseDto;
import monochrome.libri.notification.repository.NotificationRepository;
import monochrome.libri.notification.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private FcmPushSender fcmPushSender;

    @InjectMocks
    private NotificationServiceImpl service;

    @Test
    @DisplayName("자신의 노트에 발생한 행위는 알림을 생성하지 않는다")
    void createNotification_ignoresSelfNotification() {
        service.createNotification(1L, 1L, "/profile/1", 10L, "content", NotificationType.LIKED);

        verify(notificationRepository, never()).save(any());
        verifyNoInteractions(fcmPushSender);
    }

    @Test
    @DisplayName("알림을 올바른 필드값으로 저장한다")
    void createNotification_savesWithCorrectFields() {
        Notification saved = Notification.builder().id(100L).build();
        when(notificationRepository.save(any())).thenReturn(saved);

        service.createNotification(2L, 1L, "/profile/1", 10L, "좋아요를 눌렀습니다.", NotificationType.LIKED);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        Notification captured = captor.getValue();
        assertThat(captured.getRecipientMemberId()).isEqualTo(2L);
        assertThat(captured.getActorMemberId()).isEqualTo(1L);
        assertThat(captured.getActorProfilePath()).isEqualTo("/profile/1");
        assertThat(captured.getNoteId()).isEqualTo(10L);
        assertThat(captured.getContent()).isEqualTo("좋아요를 눌렀습니다.");
        assertThat(captured.getNotificationType()).isEqualTo(NotificationType.LIKED);
    }

    @Test
    @DisplayName("FCM 전송 시 notificationId, noteId, type을 payload에 담는다")
    void createNotification_sendsCorrectFcmPayload() {
        Notification saved = Notification.builder().id(100L).build();
        when(notificationRepository.save(any())).thenReturn(saved);

        service.createNotification(2L, 1L, "/profile/1", 10L, "댓글을 남겼습니다.", NotificationType.COMMENT);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, String>> dataCaptor = ArgumentCaptor.forClass(Map.class);
        verify(fcmPushSender).sendToMember(eq(2L), eq("Libri"), eq("댓글을 남겼습니다."), dataCaptor.capture());
        Map<String, String> data = dataCaptor.getValue();
        assertThat(data).containsEntry("notificationId", "100");
        assertThat(data).containsEntry("noteId", "10");
        assertThat(data).containsEntry("type", "COMMENT");
    }

    @Test
    @DisplayName("행위자 프로필 경로가 null이면 빈 문자열로 저장한다")
    void createNotification_usesEmptyStringWhenProfilePathNull() {
        Notification saved = Notification.builder().id(1L).build();
        when(notificationRepository.save(any())).thenReturn(saved);

        service.createNotification(2L, 1L, null, 10L, "content", NotificationType.LIKED);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().getActorProfilePath()).isEqualTo("");
    }

    @Test
    @DisplayName("FCM 전송이 실패해도 알림 저장은 완료된다")
    void createNotification_continuesWhenFcmFails() {
        Notification saved = Notification.builder().id(1L).build();
        when(notificationRepository.save(any())).thenReturn(saved);
        doThrow(new RuntimeException("FCM unavailable"))
                .when(fcmPushSender).sendToMember(anyLong(), any(), any(), any());

        service.createNotification(2L, 1L, "/profile/1", 10L, "content", NotificationType.LIKED);

        verify(notificationRepository).save(any());
    }

    @Test
    @DisplayName("알림 목록을 Slice DTO로 변환하여 반환한다")
    void getNotifications_returnsMappedSlice() {
        Notification n = Notification.builder()
                .id(1L)
                .recipientMemberId(1L)
                .actorMemberId(2L)
                .actorProfilePath("/profile/2")
                .noteId(10L)
                .content("좋아요를 눌렀습니다.")
                .notificationType(NotificationType.LIKED)
                .build();
        Pageable pageable = PageRequest.of(0, 10);
        when(notificationRepository.findByRecipientMemberIdAndDeletedFalseOrderByCreatedDateDesc(1L, pageable))
                .thenReturn(new SliceImpl<>(List.of(n), pageable, false));
        when(notificationRepository.countByRecipientMemberIdAndDeletedFalse(1L)).thenReturn(1L);

        NotificationSliceResponseDto result = service.getNotifications(1L, pageable);

        assertThat(result.totalCount()).isEqualTo(1L);
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).noteId()).isEqualTo(10L);
        assertThat(result.content().get(0).actorMemberId()).isEqualTo(2L);
        assertThat(result.hasNext()).isFalse();
        assertThat(result.page()).isEqualTo(0);
        assertThat(result.size()).isEqualTo(10);
    }

    @Test
    @DisplayName("존재하지 않는 알림을 읽음 처리하면 예외를 던진다")
    void readNotification_throwsWhenNotFound() {
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.readNotification(999L, 1L))
                .isInstanceOf(LibriException.class);
    }

    @Test
    @DisplayName("자신의 알림이 아닌 경우 읽음 처리 시 예외를 던진다")
    void readNotification_throwsWhenNotOwner() {
        Notification n = Notification.builder()
                .id(1L)
                .recipientMemberId(2L)
                .build();
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(n));

        assertThatThrownBy(() -> service.readNotification(1L, 999L))
                .isInstanceOf(LibriException.class);
    }

    @Test
    @DisplayName("읽음 처리 후 알림의 read 상태가 true가 된다")
    void readNotification_marksAsRead() {
        Notification n = Notification.builder()
                .id(1L)
                .recipientMemberId(1L)
                .build();
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(n));

        service.readNotification(1L, 1L);

        assertThat(n.isRead()).isTrue();
    }

    @Test
    @DisplayName("전체 읽음 처리 시 읽지 않은 모든 알림에 적용된다")
    void readAllNotifications_marksAllUnread() {
        Notification n1 = Notification.builder().id(1L).recipientMemberId(1L).build();
        Notification n2 = Notification.builder().id(2L).recipientMemberId(1L).build();
        when(notificationRepository.findByRecipientMemberIdAndReadFalseAndDeletedFalse(1L))
                .thenReturn(List.of(n1, n2));

        service.readAllNotifications(1L);

        assertThat(n1.isRead()).isTrue();
        assertThat(n2.isRead()).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 알림을 삭제하면 예외를 던진다")
    void deleteNotification_throwsWhenNotFound() {
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteNotification(999L, 1L))
                .isInstanceOf(LibriException.class);
    }

    @Test
    @DisplayName("자신의 알림이 아닌 경우 삭제 시 예외를 던진다")
    void deleteNotification_throwsWhenNotOwner() {
        Notification n = Notification.builder()
                .id(1L)
                .recipientMemberId(2L)
                .build();
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(n));

        assertThatThrownBy(() -> service.deleteNotification(1L, 999L))
                .isInstanceOf(LibriException.class);
    }

    @Test
    @DisplayName("삭제 후 알림의 deleted 상태가 true가 된다")
    void deleteNotification_marksAsDeleted() {
        Notification n = Notification.builder()
                .id(1L)
                .recipientMemberId(1L)
                .build();
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(n));

        service.deleteNotification(1L, 1L);

        assertThat(n.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("전체 삭제 시 모든 알림에 소프트 삭제가 적용된다")
    void deleteAllNotifications_marksAllAsDeleted() {
        Notification n1 = Notification.builder().id(1L).recipientMemberId(1L).build();
        Notification n2 = Notification.builder().id(2L).recipientMemberId(1L).build();
        when(notificationRepository.findByRecipientMemberIdAndDeletedFalse(1L))
                .thenReturn(List.of(n1, n2));

        service.deleteAllNotifications(1L);

        assertThat(n1.isDeleted()).isTrue();
        assertThat(n2.isDeleted()).isTrue();
    }
}
