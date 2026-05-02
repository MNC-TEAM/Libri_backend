package monochrome.libri.storage.controller;

import jakarta.validation.Valid;
import monochrome.libri.global.exception.ErrorCode;
import monochrome.libri.global.exception.LibriException;
import monochrome.libri.global.response.ApiResponse;
import monochrome.libri.global.security.UserPrincipal;
import monochrome.libri.global.swagger.ApiErrorCodes;
import monochrome.libri.storage.dto.request.PresignedUploadRequestDto;
import monochrome.libri.storage.dto.response.PresignedDownloadResponseDto;
import monochrome.libri.storage.dto.response.PresignedUploadResponseDto;
import monochrome.libri.storage.service.PresignedUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/files")
@SecurityRequirement(name = "BearerAuth")
public class FileController {

    private final PresignedUploadService presignedUploadService;

    public FileController(PresignedUploadService presignedUploadService) {
        this.presignedUploadService = presignedUploadService;
    }

    @PostMapping("/presigned-upload")
    @Operation(
            summary = "S3 업로드용 presigned URL 발급",
            description = "클라이언트가 S3에 직접 업로드할 수 있는 presigned URL을 발급합니다."
    )
    @ApiErrorCodes({
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.INVALID_INPUT_VALUE
    })
    public ResponseEntity<ApiResponse<PresignedUploadResponseDto>> createPresignedUploadUrl(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody PresignedUploadRequestDto request
    ) {
        if (userPrincipal == null) {
            throw new LibriException(ErrorCode.AUTHENTICATION_FAILED);
        }
        return ResponseEntity.ok(ApiResponse.ok(
                presignedUploadService.createPresignedUploadUrl(userPrincipal.getMemberId(), request)
        ));
    }

    @GetMapping("/presigned-download")
    @Operation(
            summary = "S3 조회용 presigned URL 발급",
            description = "저장된 파일 URL을 기반으로 브라우저에서 조회 가능한 presigned GET URL을 발급합니다."
    )
    @ApiErrorCodes({
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.INVALID_INPUT_VALUE
    })
    public ResponseEntity<ApiResponse<PresignedDownloadResponseDto>> createPresignedDownloadUrl(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam String fileUrl
    ) {
        if (userPrincipal == null) {
            throw new LibriException(ErrorCode.AUTHENTICATION_FAILED);
        }
        return ResponseEntity.ok(ApiResponse.ok(
                presignedUploadService.createPresignedDownloadUrl(userPrincipal.getMemberId(), fileUrl)
        ));
    }
}
