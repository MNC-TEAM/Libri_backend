package monochrome.libri.notice.controller;

import monochrome.libri.global.exception.ErrorCode;
import monochrome.libri.global.exception.LibriException;
import monochrome.libri.global.response.ApiResponse;
import monochrome.libri.global.swagger.ApiErrorCodes;
import monochrome.libri.notice.dto.response.NoticeDetailResponseDto;
import monochrome.libri.notice.dto.response.NoticeListResponseDto;
import monochrome.libri.notice.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notices")
public class NoticeController {

    private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @GetMapping
    @Operation(summary = "공지사항 목록 조회", description = "공개된 공지사항 목록을 조회합니다.")
    @ApiErrorCodes({
            ErrorCode.INVALID_INPUT_VALUE
    })
    public ResponseEntity<ApiResponse<NoticeListResponseDto>> getNotices(
            @Parameter(description = "페이지(0부터)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        if (page < 0 || size <= 0) {
            throw new LibriException(ErrorCode.INVALID_INPUT_VALUE);
        }
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.ok(noticeService.getPublishedNotices(pageable)));
    }

    @GetMapping("/{noticeId}")
    @Operation(summary = "공지사항 상세 조회", description = "공개된 공지사항 상세를 조회합니다.")
    @ApiErrorCodes({
            ErrorCode.NOTICE_NOT_FOUND
    })
    public ResponseEntity<ApiResponse<NoticeDetailResponseDto>> getNotice(@PathVariable long noticeId) {
        return ResponseEntity.ok(ApiResponse.ok(noticeService.getPublishedNotice(noticeId)));
    }
}
