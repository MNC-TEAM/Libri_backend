package monochrome.libri.member.controller;

import monochrome.libri.global.exception.ErrorCode;
import monochrome.libri.global.exception.LibriException;
import monochrome.libri.global.response.ApiResponse;
import monochrome.libri.global.security.UserPrincipal;
import monochrome.libri.global.swagger.ApiErrorCodes;
import monochrome.libri.block.dto.response.BlockedMemberListResponseDto;
import monochrome.libri.block.service.BlockService;
import monochrome.libri.member.dto.request.MemberUpdateRequestDto;
import monochrome.libri.member.dto.request.NicknameUpdateRequestDto;
import monochrome.libri.member.dto.request.PrivacyUpdateRequestDto;
import monochrome.libri.member.dto.request.ProfileImageUpdateRequestDto;
import monochrome.libri.member.dto.response.MemberPrivacyResponseDto;
import monochrome.libri.member.dto.response.MemberProfileResponseDto;
import monochrome.libri.member.dto.response.MemberResponseDto;
import monochrome.libri.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/members")
@SecurityRequirement(name = "BearerAuth")
public class MemberController {
    private final MemberService memberService;
    private final BlockService blockService;

    public MemberController(MemberService memberService, BlockService blockService) {
        this.memberService = memberService;
        this.blockService = blockService;
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

        return ResponseEntity.ok(ApiResponse.ok(memberService.getMyProfile(userPrincipal.getMemberId())));
    }

    @GetMapping("/me/privacy")
    @Operation(
            summary = "내 비공개 여부 조회",
            description = "로그인한 회원의 계정 비공개 여부를 조회합니다."
    )
    @ApiErrorCodes({
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.MEMBER_NOT_FOUND
    })
    public ResponseEntity<ApiResponse<MemberPrivacyResponseDto>> getMyPrivacy(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        if (userPrincipal == null) {
            throw new LibriException(ErrorCode.AUTHENTICATION_FAILED);
        }
        return ResponseEntity.ok(ApiResponse.ok(memberService.getMyPrivacy(userPrincipal.getMemberId())));
    }

    @GetMapping("/me/blocks")
    @Operation(
            summary = "차단한 회원 목록 조회",
            description = "로그인한 회원이 차단한 회원 목록을 조회합니다."
    )
    @ApiErrorCodes({
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.INVALID_INPUT_VALUE,
            ErrorCode.MEMBER_NOT_FOUND
    })
    public ResponseEntity<ApiResponse<BlockedMemberListResponseDto>> getMyBlockedMembers(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        if (userPrincipal == null) {
            throw new LibriException(ErrorCode.AUTHENTICATION_FAILED);
        }
        if (page < 0 || size <= 0) {
            throw new LibriException(ErrorCode.INVALID_INPUT_VALUE);
        }
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.ok(blockService.getBlockedMembers(userPrincipal.getMemberId(), pageable)));
    }

    @GetMapping("/{memberId}")
    @Operation(
            summary = "회원 프로필 조회",
            description = "다른 회원의 프로필 정보를 조회합니다. 로그인 상태면 본인 여부와 팔로우 여부를 함께 반환합니다."
    )
    @ApiErrorCodes({
            ErrorCode.MEMBER_NOT_FOUND
    })
    public ResponseEntity<ApiResponse<MemberProfileResponseDto>> getMemberProfile(
            @PathVariable long memberId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long actorMemberId = userPrincipal == null ? null : userPrincipal.getMemberId();
        return ResponseEntity.ok(ApiResponse.ok(memberService.getMemberProfile(actorMemberId, memberId)));
    }

    @PostMapping("/{memberId}/block")
    @Operation(
            summary = "회원 차단",
            description = "특정 회원을 차단합니다."
    )
    @ApiErrorCodes({
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.MEMBER_NOT_FOUND,
            ErrorCode.SELF_BLOCK_NOT_ALLOWED,
            ErrorCode.BLOCK_ALREADY_EXISTS
    })
    public ResponseEntity<ApiResponse<Void>> blockMember(
            @PathVariable long memberId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        if (userPrincipal == null) {
            throw new LibriException(ErrorCode.AUTHENTICATION_FAILED);
        }
        blockService.blockMember(userPrincipal.getMemberId(), memberId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @DeleteMapping("/{memberId}/block")
    @Operation(
            summary = "회원 차단 해제",
            description = "차단한 회원을 차단 해제합니다."
    )
    @ApiErrorCodes({
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.MEMBER_NOT_FOUND,
            ErrorCode.SELF_BLOCK_NOT_ALLOWED,
            ErrorCode.BLOCK_NOT_FOUND
    })
    public ResponseEntity<ApiResponse<Void>> unblockMember(
            @PathVariable long memberId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        if (userPrincipal == null) {
            throw new LibriException(ErrorCode.AUTHENTICATION_FAILED);
        }
        blockService.unblockMember(userPrincipal.getMemberId(), memberId);
        return ResponseEntity.ok(ApiResponse.ok());
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
        return ResponseEntity.ok(ApiResponse.ok(memberService.getMyProfile(updated.getId())));
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
        return ResponseEntity.ok(ApiResponse.ok(memberService.getMyProfile(updated.getId())));
    }

    @PatchMapping("/me/profile-image")
    @Operation(
            summary = "프로필 이미지 경로 저장",
            description = "S3 업로드 후 프로필 이미지 경로를 회원 정보에 저장합니다."
    )
    @ApiErrorCodes({
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.MEMBER_NOT_FOUND,
            ErrorCode.INVALID_INPUT_VALUE
    })
    public ResponseEntity<ApiResponse<MemberResponseDto>> updateProfileImage(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody ProfileImageUpdateRequestDto request
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
                request.profilePath(),
                null,
                null,
                null
        );
        var updated = memberService.updateMember(userPrincipal.getMemberId(), updateRequest);
        return ResponseEntity.ok(ApiResponse.ok(memberService.getMyProfile(updated.getId())));
    }
}
