package monochrome.libri.firebase;

import java.util.Map;

@FunctionalInterface
public interface FcmPushSender {

    /**
     * 수신 회원에 등록된 FCM 토큰이 있으면 푸시를 보냅니다. 실패해도 호출부 트랜잭션에는 영향이 없도록 구현합니다.
     */
    void sendToMember(long recipientMemberId, String title, String body, Map<String, String> data);
}
