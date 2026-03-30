package monochrome.libri.inquiry.controller;

import jakarta.validation.Valid;
import monochrome.libri.global.exception.ErrorCode;
import monochrome.libri.global.exception.LibriException;
import monochrome.libri.global.response.ApiResponse;
import monochrome.libri.global.security.UserPrincipal;
import monochrome.libri.global.swagger.ApiErrorCodes;
import monochrome.libri.inquiry.dto.request.InquiryAnswerRequestDto;
import monochrome.libri.inquiry.dto.request.InquiryCreateRequestDto;
import monochrome.libri.inquiry.dto.request.InquiryStatusUpdateRequestDto;
import monochrome.libri.inquiry.dto.response.InquiryDetailResponseDto;
import monochrome.libri.inquiry.dto.response.InquiryListResponseDto;
import monochrome.libri.inquiry.service.InquiryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class InquiryController {

    private final InquiryService inquiryService;

    public InquiryController(InquiryService inquiryService) {
        this.inquiryService = inquiryService;
    }

    @PostMapping("/inquiries")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "문의 작성", description = "로그인한 회원이 문의를 작성합니다.")
    @ApiErrorCodes({
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.MEMBER_NOT_FOUND,
            ErrorCode.INVALID_INPUT_VALUE
    })
    public ResponseEntity<ApiResponse<InquiryDetailResponseDto>> createInquiry(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody InquiryCreateRequestDto request
    ) {
        long memberId = userPrincipal == null ? 0L : userPrincipal.getMemberId();
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(inquiryService.createInquiry(memberId, request)));
    }

    @GetMapping("/inquiries/me")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "내 문의 목록 조회", description = "로그인한 회원의 문의 목록을 조회합니다.")
    @ApiErrorCodes({
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.INVALID_INPUT_VALUE
    })
    public ResponseEntity<ApiResponse<InquiryListResponseDto>> getMyInquiries(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "페이지(0부터)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        validatePage(page, size);
        long memberId = userPrincipal == null ? 0L : userPrincipal.getMemberId();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDate"));
        return ResponseEntity.ok(ApiResponse.ok(inquiryService.getMyInquiries(memberId, pageable)));
    }

    @GetMapping("/inquiries/{inquiryId}")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "문의 상세 조회", description = "본인 문의 또는 관리자는 문의 상세를 조회할 수 있습니다.")
    @ApiErrorCodes({
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.ACCESS_DENIED,
            ErrorCode.MEMBER_NOT_FOUND,
            ErrorCode.INQUIRY_NOT_FOUND
    })
    public ResponseEntity<ApiResponse<InquiryDetailResponseDto>> getInquiry(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable long inquiryId
    ) {
        long memberId = userPrincipal == null ? 0L : userPrincipal.getMemberId();
        return ResponseEntity.ok(ApiResponse.ok(inquiryService.getInquiry(inquiryId, memberId)));
    }

    @GetMapping("/admin/inquiries")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "문의 전체 조회", description = "관리자가 전체 문의 목록을 조회합니다.")
    @ApiErrorCodes({
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.ACCESS_DENIED,
            ErrorCode.MEMBER_NOT_FOUND,
            ErrorCode.INVALID_INPUT_VALUE
    })
    public ResponseEntity<ApiResponse<InquiryListResponseDto>> getAllInquiries(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "페이지(0부터)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        validatePage(page, size);
        long memberId = userPrincipal == null ? 0L : userPrincipal.getMemberId();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDate"));
        return ResponseEntity.ok(ApiResponse.ok(inquiryService.getAllInquiries(memberId, pageable)));
    }

    @PatchMapping("/admin/inquiries/{inquiryId}/answer")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "문의 답변 등록", description = "관리자가 문의에 답변을 등록합니다.")
    @ApiErrorCodes({
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.ACCESS_DENIED,
            ErrorCode.MEMBER_NOT_FOUND,
            ErrorCode.INQUIRY_NOT_FOUND,
            ErrorCode.INVALID_INPUT_VALUE
    })
    public ResponseEntity<ApiResponse<InquiryDetailResponseDto>> answerInquiry(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable long inquiryId,
            @Valid @RequestBody InquiryAnswerRequestDto request
    ) {
        long memberId = userPrincipal == null ? 0L : userPrincipal.getMemberId();
        return ResponseEntity.ok(ApiResponse.ok(inquiryService.answerInquiry(memberId, inquiryId, request)));
    }

    @PatchMapping("/admin/inquiries/{inquiryId}/status")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "문의 상태 변경", description = "관리자가 문의 상태를 변경합니다.")
    @ApiErrorCodes({
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.ACCESS_DENIED,
            ErrorCode.MEMBER_NOT_FOUND,
            ErrorCode.INQUIRY_NOT_FOUND,
            ErrorCode.INVALID_INPUT_VALUE
    })
    public ResponseEntity<ApiResponse<InquiryDetailResponseDto>> updateInquiryStatus(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable long inquiryId,
            @RequestBody InquiryStatusUpdateRequestDto request
    ) {
        long memberId = userPrincipal == null ? 0L : userPrincipal.getMemberId();
        return ResponseEntity.ok(ApiResponse.ok(inquiryService.updateInquiryStatus(memberId, inquiryId, request)));
    }

    private void validatePage(int page, int size) {
        if (page < 0 || size <= 0) {
            throw new LibriException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }
}
