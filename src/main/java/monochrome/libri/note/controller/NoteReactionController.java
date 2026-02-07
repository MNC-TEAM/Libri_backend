package monochrome.libri.note.controller;

import monochrome.libri.global.exception.ErrorCode;
import monochrome.libri.global.response.ApiResponse;
import monochrome.libri.global.security.UserPrincipal;
import monochrome.libri.global.swagger.ApiErrorCodes;
import monochrome.libri.note.service.NoteReactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notes/{noteId}")
@SecurityRequirement(name = "BearerAuth")
public class NoteReactionController {

    private final NoteReactionService noteReactionService;

    public NoteReactionController(NoteReactionService noteReactionService) {
        this.noteReactionService = noteReactionService;
    }

    @PostMapping("/likes")
    @Operation(
            summary = "노트 좋아요",
            description = "노트에 좋아요를 추가합니다."
    )
    @ApiErrorCodes({
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.MEMBER_NOT_FOUND,
            ErrorCode.NOTE_NOT_FOUND,
            ErrorCode.ACCESS_DENIED
    })
    public ResponseEntity<ApiResponse<Void>> like(
            @PathVariable long noteId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        long memberId = userPrincipal == null ? 0L : userPrincipal.getMemberId();
        noteReactionService.like(noteId, memberId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok());
    }

    @DeleteMapping("/likes")
    @Operation(
            summary = "노트 좋아요 취소",
            description = "노트 좋아요를 취소합니다."
    )
    @ApiErrorCodes({
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.NOTE_NOT_FOUND,
            ErrorCode.ACCESS_DENIED
    })
    public ResponseEntity<ApiResponse<Void>> unlike(
            @PathVariable long noteId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        long memberId = userPrincipal == null ? 0L : userPrincipal.getMemberId();
        noteReactionService.unlike(noteId, memberId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @PostMapping("/bookmarks")
    @Operation(
            summary = "노트 북마크",
            description = "노트를 북마크에 추가합니다."
    )
    @ApiErrorCodes({
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.MEMBER_NOT_FOUND,
            ErrorCode.NOTE_NOT_FOUND,
            ErrorCode.ACCESS_DENIED
    })
    public ResponseEntity<ApiResponse<Void>> bookmark(
            @PathVariable long noteId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        long memberId = userPrincipal == null ? 0L : userPrincipal.getMemberId();
        noteReactionService.bookmark(noteId, memberId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok());
    }

    @DeleteMapping("/bookmarks")
    @Operation(
            summary = "노트 북마크 취소",
            description = "노트 북마크를 해제합니다."
    )
    @ApiErrorCodes({
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.NOTE_NOT_FOUND,
            ErrorCode.ACCESS_DENIED
    })
    public ResponseEntity<ApiResponse<Void>> unbookmark(
            @PathVariable long noteId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        long memberId = userPrincipal == null ? 0L : userPrincipal.getMemberId();
        noteReactionService.unbookmark(noteId, memberId);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
