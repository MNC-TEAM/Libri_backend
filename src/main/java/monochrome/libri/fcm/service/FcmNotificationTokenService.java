package monochrome.libri.fcm.service;

import monochrome.libri.fcm.dto.FcmTokenRequestDto;
import monochrome.libri.member.service.MemberService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FcmNotificationTokenService {

    private final MemberService memberService;

    public FcmNotificationTokenService(MemberService memberService) {
        this.memberService = memberService;
    }

    @Transactional
    public void subscribe(long memberId, FcmTokenRequestDto request) {
        memberService.updateFcmRegistrationToken(memberId, request.fcmToken());
    }

    @Transactional
    public void clearAll(long memberId) {
        memberService.updateFcmRegistrationToken(memberId, null);
    }
}
