package monochrome.libri.comment.controller;

import jakarta.validation.Valid;
import monochrome.libri.comment.dto.request.CommentCreateRequestDto;
import monochrome.libri.comment.dto.response.CommentSliceResponseDto;
import monochrome.libri.comment.service.NoteCommentService;
import monochrome.libri.global.exception.ErrorCode;
import monochrome.libri.global.exception.LibriException;
import monochrome.libri.global.response.ApiResponse;
import monochrome.libri.global.security.UserPrincipal;
import monochrome.libri.global.swagger.ApiErrorCodes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notes/{noteId}/comments")
public class NoteCommentController {

    private final NoteCommentService commentService;

    public NoteCommentController(NoteCommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    @Operation(
            summary = "노트 댓글 목록 조회",
            description = "노트에 달린 댓글을 페이지네이션으로 조회합니다."
    )
    @ApiErrorCodes({
            ErrorCode.NOTE_NOT_FOUND,
            ErrorCode.ACCESS_DENIED,
            ErrorCode.PAGINATION_INVALID
    })
    public ResponseEntity<ApiResponse<CommentSliceResponseDto>> getComments(
            @PathVariable long noteId,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "페이지(0부터)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        if (page < 0 || size <= 0) {
            throw new LibriException(ErrorCode.PAGINATION_INVALID);
        }
        Pageable pageable = PageRequest.of(page, size);
        long memberId = userPrincipal == null ? 0L : userPrincipal.getMemberId();
        CommentSliceResponseDto response = commentService.getComments(noteId, memberId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping
    @Operation(
            summary = "노트 댓글 작성",
            description = "노트에 댓글을 작성합니다."
    )
    @SecurityRequirement(name = "BearerAuth")
    @ApiErrorCodes({
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.MEMBER_NOT_FOUND,
            ErrorCode.NOTE_NOT_FOUND,
            ErrorCode.ACCESS_DENIED,
            ErrorCode.INVALID_INPUT_VALUE
    })
    public ResponseEntity<ApiResponse<Void>> createComment(
            @PathVariable long noteId,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody CommentCreateRequestDto request
    ) {
        long memberId = userPrincipal == null ? 0L : userPrincipal.getMemberId();
        commentService.createComment(noteId, memberId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok());
    }

    @DeleteMapping("/{commentId}")
    @Operation(
            summary = "노트 댓글 삭제",
            description = "본인이 작성한 댓글을 삭제합니다."
    )
    @SecurityRequirement(name = "BearerAuth")
    @ApiErrorCodes({
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.COMMENT_NOT_FOUND,
            ErrorCode.ACCESS_DENIED
    })
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable long noteId,
            @PathVariable long commentId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        long memberId = userPrincipal == null ? 0L : userPrincipal.getMemberId();
        commentService.deleteComment(noteId, commentId, memberId);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
