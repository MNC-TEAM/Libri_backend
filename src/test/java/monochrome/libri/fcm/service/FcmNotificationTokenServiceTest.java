package monochrome.libri.fcm.service;

import monochrome.libri.fcm.dto.FcmTokenRequestDto;
import monochrome.libri.member.service.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FcmNotificationTokenServiceTest {

    @Mock
    private MemberService memberService;

    @InjectMocks
    private FcmNotificationTokenService service;

    @Test
    @DisplayName("FCM 토큰 등록을 MemberService에 위임한다")
    void subscribe_delegatesTokenToMemberService() {
        FcmTokenRequestDto request = new FcmTokenRequestDto("device-token-abc");

        service.subscribe(1L, request);

        verify(memberService).updateFcmRegistrationToken(1L, "device-token-abc");
    }

    @Test
    @DisplayName("전체 토큰 삭제를 MemberService에 null로 위임한다")
    void clearAll_delegatesNullToMemberService() {
        service.clearAll(1L);

        verify(memberService).updateFcmRegistrationToken(1L, null);
    }
}
