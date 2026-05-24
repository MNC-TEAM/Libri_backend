package monochrome.libri.fcm.controller;

import monochrome.libri.fcm.dto.FcmTokenRequestDto;
import monochrome.libri.fcm.service.FcmNotificationTokenService;
import monochrome.libri.global.exception.ErrorCode;
import monochrome.libri.global.exception.LibriException;
import monochrome.libri.global.response.ApiResponse;
import monochrome.libri.global.security.UserPrincipal;
import monochrome.libri.global.swagger.ApiErrorCodes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
@SecurityRequirement(name = "BearerAuth")
public class FcmNotificationTokenController {

    private final FcmNotificationTokenService fcmNotificationTokenService;

    public FcmNotificationTokenController(FcmNotificationTokenService fcmNotificationTokenService) {
        this.fcmNotificationTokenService = fcmNotificationTokenService;
    }

    @PostMapping("/fcm-tokens")
    @Operation(
            summary = "FCM 토큰 구독(등록)",
            description = "요청 본문의 fcmToken 을 현재 로그인 회원에 연결합니다."
    )
    @ApiErrorCodes({
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.MEMBER_NOT_FOUND,
            ErrorCode.INVALID_INPUT_VALUE
    })
    public ResponseEntity<ApiResponse<Void>> subscribe(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody FcmTokenRequestDto request
    ) {
        if (userPrincipal == null) {
            throw new LibriException(ErrorCode.AUTHENTICATION_FAILED);
        }
        fcmNotificationTokenService.subscribe(userPrincipal.getMemberId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok());
    }

    @DeleteMapping("/fcm-tokens")
    @Operation(
            summary = "FCM 토큰 전체 삭제",
            description = "현재 로그인 회원에 연결된 모든 FCM 등록 토큰을 삭제합니다."
    )
    @ApiErrorCodes({
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.MEMBER_NOT_FOUND
    })
    public ResponseEntity<ApiResponse<Void>> clearAll(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        if (userPrincipal == null) {
            throw new LibriException(ErrorCode.AUTHENTICATION_FAILED);
        }
        fcmNotificationTokenService.clearAll(userPrincipal.getMemberId());
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
