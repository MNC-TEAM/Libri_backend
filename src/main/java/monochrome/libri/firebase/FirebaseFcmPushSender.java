package monochrome.libri.firebase;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import monochrome.libri.fcm.repository.FcmNotificationTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public final class FirebaseFcmPushSender implements FcmPushSender {

    private static final Logger log = LoggerFactory.getLogger(FirebaseFcmPushSender.class);

    private final FirebaseApp firebaseApp;
    private final FcmNotificationTokenRepository fcmNotificationTokenRepository;

    public FirebaseFcmPushSender(FirebaseApp firebaseApp, FcmNotificationTokenRepository fcmNotificationTokenRepository) {
        this.firebaseApp = firebaseApp;
        this.fcmNotificationTokenRepository = fcmNotificationTokenRepository;
    }

    @Override
    public void sendToMember(long recipientMemberId, String title, String body, Map<String, String> data) {
        fcmNotificationTokenRepository.findByMember_IdOrderByLastUsedDateDesc(recipientMemberId)
                .forEach(row -> sendOne(row.getToken(), recipientMemberId, title, body, data));
    }

    private void sendOne(String token, long recipientMemberId, String title, String body, Map<String, String> data) {
        if (token == null || token.isBlank()) {
            return;
        }
        try {
            Message message = Message.builder()
                    .setToken(token.trim())
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .putAllData(data != null ? data : Map.of())
                    .build();
            FirebaseMessaging.getInstance(firebaseApp).send(message);
        } catch (FirebaseMessagingException e) {
            log.warn(
                    "FCM 전송 실패 recipientMemberId={} error={}",
                    recipientMemberId,
                    e.getMessagingErrorCode() != null ? e.getMessagingErrorCode() : e.getMessage()
            );
        }
    }
}
