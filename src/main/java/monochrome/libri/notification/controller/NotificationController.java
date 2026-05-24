package monochrome.libri.notification.controller;

import monochrome.libri.global.exception.ErrorCode;
import monochrome.libri.global.exception.LibriException;
import monochrome.libri.global.response.ApiResponse;
import monochrome.libri.global.security.UserPrincipal;
import monochrome.libri.global.swagger.ApiErrorCodes;
import monochrome.libri.notification.dto.response.NotificationSliceResponseDto;
import monochrome.libri.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@SecurityRequirement(name = "BearerAuth")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @Operation(
            summary = "알림 목록 조회",
            description = "본인이 받은 알림을 페이지네이션으로 조회합니다."
    )
    @ApiErrorCodes({
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.INVALID_INPUT_VALUE
    })
    public ResponseEntity<ApiResponse<NotificationSliceResponseDto>> getNotifications(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "페이지(0부터)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        if (userPrincipal == null) {
            throw new LibriException(ErrorCode.AUTHENTICATION_FAILED);
        }
        if (page < 0 || size <= 0) {
            throw new LibriException(ErrorCode.INVALID_INPUT_VALUE);
        }
        long memberId = userPrincipal.getMemberId();
        Pageable pageable = PageRequest.of(page, size);
        NotificationSliceResponseDto response = notificationService.getNotifications(memberId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PatchMapping("/{notificationId}/read")
    @Operation(
            summary = "알림 읽음 처리",
            description = "단일 알림을 읽음 처리합니다."
    )
    @ApiErrorCodes({
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.NOTIFICATION_NOT_FOUND,
            ErrorCode.ACCESS_DENIED
    })
    public ResponseEntity<ApiResponse<Void>> readNotification(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable long notificationId
    ) {
        if (userPrincipal == null) {
            throw new LibriException(ErrorCode.AUTHENTICATION_FAILED);
        }
        long memberId = userPrincipal.getMemberId();
        notificationService.readNotification(notificationId, memberId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @PatchMapping("/read-all")
    @Operation(
            summary = "알림 전체 읽음 처리",
            description = "본인의 모든 알림을 읽음 처리합니다."
    )
    @ApiErrorCodes({
            ErrorCode.AUTHENTICATION_FAILED
    })
    public ResponseEntity<ApiResponse<Void>> readAllNotifications(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        if (userPrincipal == null) {
            throw new LibriException(ErrorCode.AUTHENTICATION_FAILED);
        }
        long memberId = userPrincipal.getMemberId();
        notificationService.readAllNotifications(memberId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @DeleteMapping("/{notificationId}")
    @Operation(
            summary = "알림 삭제",
            description = "본인이 받은 알림을 삭제합니다."
    )
    @ApiErrorCodes({
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.NOTIFICATION_NOT_FOUND,
            ErrorCode.ACCESS_DENIED
    })
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable long notificationId
    ) {
        if (userPrincipal == null) {
            throw new LibriException(ErrorCode.AUTHENTICATION_FAILED);
        }
        long memberId = userPrincipal.getMemberId();
        notificationService.deleteNotification(notificationId, memberId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @DeleteMapping("/all")
    @Operation(
            summary = "알림 전체 삭제",
            description = "본인이 받은 모든 알림을 삭제합니다."
    )
    @ApiErrorCodes({
            ErrorCode.AUTHENTICATION_FAILED
    })
    public ResponseEntity<ApiResponse<Void>> deleteAllNotifications(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        if (userPrincipal == null) {
            throw new LibriException(ErrorCode.AUTHENTICATION_FAILED);
        }
        long memberId = userPrincipal.getMemberId();
        notificationService.deleteAllNotifications(memberId);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
