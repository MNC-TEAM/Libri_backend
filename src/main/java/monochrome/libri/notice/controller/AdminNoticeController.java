package monochrome.libri.notice.controller;

import jakarta.validation.Valid;
import monochrome.libri.global.exception.ErrorCode;
import monochrome.libri.global.response.ApiResponse;
import monochrome.libri.global.security.UserPrincipal;
import monochrome.libri.global.swagger.ApiErrorCodes;
import monochrome.libri.notice.dto.request.NoticeCreateRequestDto;
import monochrome.libri.notice.dto.request.NoticeUpdateRequestDto;
import monochrome.libri.notice.dto.response.NoticeDetailResponseDto;
import monochrome.libri.notice.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/notices")
@SecurityRequirement(name = "BearerAuth")
public class AdminNoticeController {

    private final NoticeService noticeService;

    public AdminNoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @PostMapping
    @Operation(summary = "공지사항 등록", description = "관리자가 공지사항을 등록합니다.")
    @ApiErrorCodes({
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.ACCESS_DENIED,
            ErrorCode.MEMBER_NOT_FOUND,
            ErrorCode.INVALID_INPUT_VALUE
    })
    public ResponseEntity<ApiResponse<NoticeDetailResponseDto>> createNotice(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody NoticeCreateRequestDto request
    ) {
        long memberId = userPrincipal == null ? 0L : userPrincipal.getMemberId();
        NoticeDetailResponseDto response = noticeService.createNotice(memberId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @PatchMapping("/{noticeId}")
    @Operation(summary = "공지사항 수정", description = "관리자가 공지사항을 수정합니다.")
    @ApiErrorCodes({
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.ACCESS_DENIED,
            ErrorCode.MEMBER_NOT_FOUND,
            ErrorCode.NOTICE_NOT_FOUND,
            ErrorCode.INVALID_INPUT_VALUE
    })
    public ResponseEntity<ApiResponse<NoticeDetailResponseDto>> updateNotice(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable long noticeId,
            @RequestBody NoticeUpdateRequestDto request
    ) {
        long memberId = userPrincipal == null ? 0L : userPrincipal.getMemberId();
        return ResponseEntity.ok(ApiResponse.ok(noticeService.updateNotice(memberId, noticeId, request)));
    }

    @DeleteMapping("/{noticeId}")
    @Operation(summary = "공지사항 삭제", description = "관리자가 공지사항을 삭제합니다.")
    @ApiErrorCodes({
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.ACCESS_DENIED,
            ErrorCode.MEMBER_NOT_FOUND,
            ErrorCode.NOTICE_NOT_FOUND
    })
    public ResponseEntity<ApiResponse<Void>> deleteNotice(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable long noticeId
    ) {
        long memberId = userPrincipal == null ? 0L : userPrincipal.getMemberId();
        noticeService.deleteNotice(memberId, noticeId);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
