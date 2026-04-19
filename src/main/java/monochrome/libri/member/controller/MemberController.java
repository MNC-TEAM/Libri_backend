package monochrome.libri.member.controller;

import monochrome.libri.global.exception.ErrorCode;
import monochrome.libri.global.exception.LibriException;
import monochrome.libri.global.response.ApiResponse;
import monochrome.libri.global.security.UserPrincipal;
import monochrome.libri.global.swagger.ApiErrorCodes;
import monochrome.libri.member.dto.request.FcmRegistrationTokenRequestDto;
import monochrome.libri.member.dto.request.MemberUpdateRequestDto;
import monochrome.libri.member.dto.request.NicknameUpdateRequestDto;
import monochrome.libri.member.dto.request.PrivacyUpdateRequestDto;
import monochrome.libri.member.dto.response.MemberResponseDto;
import monochrome.libri.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/members")
@SecurityRequirement(name = "BearerAuth")
public class MemberController {
    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/me")
    @Operation(
            summary = "내 정보 조회",
            description = "액세스 토큰으로 로그인한 회원의 정보를 조회합니다."
    )
    @ApiErrorCodes({
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.MEMBER_NOT_FOUND
    })
    public ResponseEntity<ApiResponse<MemberResponseDto>> getMe(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        if (userPrincipal == null) {
            throw new LibriException(ErrorCode.AUTHENTICATION_FAILED);
        }

        var member = memberService.getMemberById(userPrincipal.getMemberId())
                .orElseThrow(() -> new LibriException(ErrorCode.MEMBER_NOT_FOUND));
        return ResponseEntity.ok(ApiResponse.ok(MemberResponseDto.from(member)));
    }

    /**
     * 회원 탈퇴 (현재 로그인한 사용자 기준)
     * DELETE /api/v1/auth/me
     */
    @DeleteMapping("/withdraw")
    @Operation(
            summary = "회원 탈퇴",
            description = "회원 탈퇴를 처리합니다."
    )
    @ApiErrorCodes({
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.MEMBER_NOT_FOUND
    })
    public ResponseEntity<ApiResponse<Void>> withdraw(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        if (userPrincipal == null) {
            throw new LibriException(ErrorCode.AUTHENTICATION_FAILED);
        }
        memberService.withdraw(userPrincipal.getMemberId());
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @PatchMapping("/me/nickname")
    @Operation(
            summary = "닉네임 변경",
            description = "로그인한 회원의 닉네임을 변경합니다."
    )
    @ApiErrorCodes({
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.MEMBER_NOT_FOUND,
            ErrorCode.INVALID_INPUT_VALUE
    })
    public ResponseEntity<ApiResponse<MemberResponseDto>> updateNickname(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody NicknameUpdateRequestDto request
    ) {
        if (userPrincipal == null) {
            throw new LibriException(ErrorCode.AUTHENTICATION_FAILED);
        }
        MemberUpdateRequestDto updateRequest = new MemberUpdateRequestDto(
                null,
                null,
                null,
                null,
                null,
                request.nickname(),
                null,
                null,
                null,
                null,
                null
        );
        var updated = memberService.updateMember(userPrincipal.getMemberId(), updateRequest);
        return ResponseEntity.ok(ApiResponse.ok(MemberResponseDto.from(updated)));
    }

    @PatchMapping("/me/privacy")
    @Operation(
            summary = "계정 비공개 설정 변경",
            description = "로그인한 회원의 계정 비공개 여부를 변경합니다."
    )
    @ApiErrorCodes({
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.MEMBER_NOT_FOUND,
            ErrorCode.INVALID_INPUT_VALUE
    })
    public ResponseEntity<ApiResponse<MemberResponseDto>> updatePrivacy(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody PrivacyUpdateRequestDto request
    ) {
        if (userPrincipal == null) {
            throw new LibriException(ErrorCode.AUTHENTICATION_FAILED);
        }
        MemberUpdateRequestDto updateRequest = new MemberUpdateRequestDto(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                request.privateAccount(),
                null,
                null
        );
        var updated = memberService.updateMember(userPrincipal.getMemberId(), updateRequest);
        return ResponseEntity.ok(ApiResponse.ok(MemberResponseDto.from(updated)));
    }

    @PatchMapping("/me/fcm-token")
    @Operation(
            summary = "FCM 등록 토큰 저장",
            description = "푸시 수신용 FCM 등록 토큰을 저장합니다. token을 비우면 삭제됩니다."
    )
    @ApiErrorCodes({
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.MEMBER_NOT_FOUND,
            ErrorCode.INVALID_INPUT_VALUE
    })
    public ResponseEntity<ApiResponse<Void>> updateFcmRegistrationToken(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody(required = false) FcmRegistrationTokenRequestDto request
    ) {
        if (userPrincipal == null) {
            throw new LibriException(ErrorCode.AUTHENTICATION_FAILED);
        }
        String token = request != null ? request.token() : null;
        memberService.updateFcmRegistrationToken(userPrincipal.getMemberId(), token);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
