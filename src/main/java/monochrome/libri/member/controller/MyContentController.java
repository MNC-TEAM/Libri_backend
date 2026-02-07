package monochrome.libri.member.controller;

import monochrome.libri.comment.dto.response.MyCommentListResponseDto;
import monochrome.libri.comment.service.NoteCommentService;
import monochrome.libri.global.exception.ErrorCode;
import monochrome.libri.global.exception.LibriException;
import monochrome.libri.global.response.ApiResponse;
import monochrome.libri.global.security.UserPrincipal;
import monochrome.libri.global.swagger.ApiErrorCodes;
import monochrome.libri.note.dto.response.NoteBookmarkListResponseDto;
import monochrome.libri.note.service.NoteService;
import monochrome.libri.review.dto.response.MyReviewListResponseDto;
import monochrome.libri.review.dto.response.ReviewBookmarkListResponseDto;
import monochrome.libri.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me")
@SecurityRequirement(name = "BearerAuth")
public class MyContentController {

    private final NoteService noteService;
    private final ReviewService reviewService;
    private final NoteCommentService noteCommentService;

    public MyContentController(
            NoteService noteService,
            ReviewService reviewService,
            NoteCommentService noteCommentService
    ) {
        this.noteService = noteService;
        this.reviewService = reviewService;
        this.noteCommentService = noteCommentService;
    }

    @GetMapping("/bookmarks/notes")
    @Operation(
            summary = "북마크한 노트 조회",
            description = "회원이 북마크한 노트 목록을 페이지네이션으로 조회합니다."
    )
    @ApiErrorCodes({
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.INVALID_INPUT_VALUE
    })
    public ResponseEntity<ApiResponse<NoteBookmarkListResponseDto>> getBookmarkedNotes(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "페이지(0부터)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        validatePage(page, size);
        long memberId = userPrincipal == null ? 0L : userPrincipal.getMemberId();
        Pageable pageable = PageRequest.of(page, size);
        NoteBookmarkListResponseDto response = noteService.getBookmarkedNotes(memberId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/bookmarks/reviews")
    @Operation(
            summary = "북마크한 리뷰 조회",
            description = "회원이 북마크한 리뷰 목록을 페이지네이션으로 조회합니다."
    )
    @ApiErrorCodes({
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.INVALID_INPUT_VALUE
    })
    public ResponseEntity<ApiResponse<ReviewBookmarkListResponseDto>> getBookmarkedReviews(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "페이지(0부터)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        validatePage(page, size);
        long memberId = userPrincipal == null ? 0L : userPrincipal.getMemberId();
        Pageable pageable = PageRequest.of(page, size);
        ReviewBookmarkListResponseDto response = reviewService.getBookmarkedReviews(memberId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/reviews")
    @Operation(
            summary = "내 리뷰 조회",
            description = "회원이 작성한 리뷰 목록을 페이지네이션으로 조회합니다."
    )
    @ApiErrorCodes({
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.INVALID_INPUT_VALUE
    })
    public ResponseEntity<ApiResponse<MyReviewListResponseDto>> getMyReviews(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "페이지(0부터)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        validatePage(page, size);
        long memberId = userPrincipal == null ? 0L : userPrincipal.getMemberId();
        Pageable pageable = PageRequest.of(page, size);
        MyReviewListResponseDto response = reviewService.getMyReviews(memberId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/comments")
    @Operation(
            summary = "내 댓글 조회",
            description = "회원이 작성한 노트 댓글 목록을 페이지네이션으로 조회합니다."
    )
    @ApiErrorCodes({
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.INVALID_INPUT_VALUE
    })
    public ResponseEntity<ApiResponse<MyCommentListResponseDto>> getMyComments(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "페이지(0부터)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        validatePage(page, size);
        long memberId = userPrincipal == null ? 0L : userPrincipal.getMemberId();
        Pageable pageable = PageRequest.of(page, size);
        MyCommentListResponseDto response = noteCommentService.getCommentsByMember(memberId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    private void validatePage(int page, int size) {
        if (page < 0 || size <= 0) {
            throw new LibriException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }
}
