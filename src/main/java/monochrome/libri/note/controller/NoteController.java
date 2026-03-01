package monochrome.libri.note.controller;

import monochrome.libri.global.exception.ErrorCode;
import monochrome.libri.global.exception.LibriException;
import monochrome.libri.global.response.ApiResponse;
import monochrome.libri.global.security.UserPrincipal;
import monochrome.libri.global.swagger.ApiErrorCodes;
import monochrome.libri.note.dto.response.NoteDetailResponseDto;
import monochrome.libri.note.dto.response.NoteListResponseDto;
import monochrome.libri.note.service.NoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notes")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping
    @Operation(
            summary = "내 노트 목록 조회",
            description = "회원이 작성한 노트 목록을 페이지네이션과 정렬 옵션으로 조회합니다."
    )
    @SecurityRequirement(name = "BearerAuth")
    @ApiErrorCodes({
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.PAGINATION_INVALID
    })
    public ResponseEntity<ApiResponse<NoteListResponseDto>> getNotes(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "페이지(0부터)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(
                    description = "정렬 기준",
                    schema = @Schema(allowableValues = {"latest", "oldest"}),
                    example = "latest"
            )
            @RequestParam(defaultValue = "latest") String sort
    ) {
        if (page < 0 || size <= 0) {
            throw new LibriException(ErrorCode.PAGINATION_INVALID);
        }

        Sort sortSpec = resolveSort(sort);
        Pageable pageable = PageRequest.of(page, size, sortSpec);
        long memberId = userPrincipal == null ? 0L : userPrincipal.getMemberId();
        NoteListResponseDto response = noteService.getNotesByMember(memberId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{noteId}")
    @Operation(
            summary = "노트 상세 조회",
            description = "노트 상세 정보를 조회합니다. 비공개 노트는 소유자만 조회할 수 있습니다."
    )
    @ApiErrorCodes({
            ErrorCode.NOTE_NOT_FOUND,
            ErrorCode.ACCESS_DENIED
    })
    public ResponseEntity<ApiResponse<NoteDetailResponseDto>> getNoteDetail(
            @PathVariable long noteId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        long memberId = userPrincipal == null ? 0L : userPrincipal.getMemberId();
        NoteDetailResponseDto response = noteService.getNoteDetail(noteId, memberId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    private Sort resolveSort(String sort) {
        if (sort == null || sort.isBlank() || "latest".equalsIgnoreCase(sort)) {
            return Sort.by(Sort.Direction.DESC, "createdDate");
        }
        if ("oldest".equalsIgnoreCase(sort)) {
            return Sort.by(Sort.Direction.ASC, "createdDate");
        }
        throw new LibriException(ErrorCode.INVALID_INPUT_VALUE);
    }
}
