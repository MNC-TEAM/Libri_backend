package monochrome.libri.shelf.controller;

import jakarta.validation.Valid;
import monochrome.libri.global.exception.ErrorCode;
import monochrome.libri.global.exception.LibriException;
import monochrome.libri.global.response.ApiResponse;
import monochrome.libri.global.security.UserPrincipal;
import monochrome.libri.global.swagger.ApiErrorCodes;
import monochrome.libri.note.dto.request.NoteCreateRequestDto;
import monochrome.libri.note.dto.request.NoteUpdateRequestDto;
import monochrome.libri.note.dto.response.NoteSliceResponseDto;
import monochrome.libri.note.dto.response.NoteSummaryResponseDto;
import monochrome.libri.note.service.NoteService;
import monochrome.libri.shelf.dto.request.ShelfCreateRequestDto;
import monochrome.libri.shelf.dto.request.ShelfUpdateRequestDto;
import monochrome.libri.shelf.dto.response.ShelfDetailResponseDto;
import monochrome.libri.shelf.service.ShelfService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;

@RestController
@RequestMapping("/api/v1/shelves")
@SecurityRequirement(name = "BearerAuth")
public class ShelfController {

    private final ShelfService shelfService;
    private final NoteService noteService;

    public ShelfController(ShelfService shelfService, NoteService noteService) {
        this.shelfService = shelfService;
        this.noteService = noteService;
    }

    @GetMapping("/{shelfId}")
    @Operation(
            summary = "서재 상세 조회",
            description = "서재의 도서 정보와 진행 정보를 조회합니다."
    )
    @ApiErrorCodes({
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.SHELF_NOT_FOUND
    })
    public ResponseEntity<ApiResponse<ShelfDetailResponseDto>> getShelfDetail(
            @PathVariable long shelfId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long memberId = userPrincipal == null ? null : userPrincipal.getMemberId();
        ShelfDetailResponseDto response = shelfService.getShelfDetail(shelfId, memberId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping
    @Operation(
            summary = "서재에 책 추가",
            description = "회원 서재에 도서를 추가합니다."
    )
    @ApiErrorCodes({
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.INVALID_INPUT_VALUE,
            ErrorCode.MEMBER_NOT_FOUND,
            ErrorCode.BOOK_NOT_FOUND
    })
    public ResponseEntity<ApiResponse<Void>> createShelf(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody ShelfCreateRequestDto request
    ) {
        long memberId = userPrincipal == null ? 0L : userPrincipal.getMemberId();
        shelfService.createShelf(memberId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok());
    }

    @PatchMapping("/{shelfId}")
    @Operation(
            summary = "서재 정보 수정",
            description = "서재 상태, 진행도, 기간 등의 정보를 수정합니다."
    )
    @ApiErrorCodes({
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.SHELF_NOT_FOUND,
            ErrorCode.INVALID_INPUT_VALUE
    })
    public ResponseEntity<ApiResponse<ShelfDetailResponseDto>> updateShelf(
            @PathVariable long shelfId,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody ShelfUpdateRequestDto request
    ) {
        Long memberId = userPrincipal == null ? null : userPrincipal.getMemberId();
        ShelfDetailResponseDto response = shelfService.updateShelf(shelfId, memberId, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{shelfId}/notes")
    @Operation(
            summary = "서재 노트 조회",
            description = "특정 서재의 노트 목록을 페이지네이션으로 조회합니다."
    )
    @ApiErrorCodes({
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.SHELF_NOT_FOUND,
            ErrorCode.INVALID_INPUT_VALUE
    })
    public ResponseEntity<ApiResponse<NoteSliceResponseDto>> getNotesByShelf(
            @PathVariable long shelfId,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "페이지(0부터)", example = "0")
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "3")
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "3") int size
    ) {
        if (page < 0 || size <= 0) {
            throw new LibriException(ErrorCode.INVALID_INPUT_VALUE);
        }
        Pageable pageable = PageRequest.of(page, size);
        long memberId = userPrincipal == null ? 0L : userPrincipal.getMemberId();
        NoteSliceResponseDto response = noteService.getNotesSliceByShelf(shelfId, memberId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/{shelfId}/notes")
    @Operation(
            summary = "서재 노트 작성",
            description = "특정 서재에 노트를 작성합니다."
    )
    @ApiErrorCodes({
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.SHELF_NOT_FOUND,
            ErrorCode.MEMBER_NOT_FOUND,
            ErrorCode.INVALID_INPUT_VALUE
    })
    public ResponseEntity<ApiResponse<Void>> createNote(
            @PathVariable long shelfId,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody NoteCreateRequestDto request
    ) {
        long memberId = userPrincipal == null ? 0L : userPrincipal.getMemberId();
        noteService.createNote(shelfId, memberId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok());
    }

    @PatchMapping("/{shelfId}/notes/{noteId}")
    @Operation(
            summary = "서재 노트 수정",
            description = "노트 내용/비공개 여부/진행 정보를 수정합니다."
    )
    @ApiErrorCodes({
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.INVALID_INPUT_VALUE,
            ErrorCode.NOTE_NOT_FOUND,
            ErrorCode.ACCESS_DENIED
    })
    public ResponseEntity<ApiResponse<NoteSummaryResponseDto>> updateNote(
            @PathVariable long shelfId,
            @PathVariable long noteId,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody NoteUpdateRequestDto request
    ) {
        long memberId = userPrincipal == null ? 0L : userPrincipal.getMemberId();
        NoteSummaryResponseDto response = noteService.updateNote(shelfId, noteId, memberId, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{shelfId}/notes/{noteId}")
    @Operation(
            summary = "서재 노트 삭제",
            description = "서재의 노트를 삭제합니다."
    )
    @ApiErrorCodes({
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.NOTE_NOT_FOUND,
            ErrorCode.ACCESS_DENIED
    })
    public ResponseEntity<ApiResponse<Void>> deleteNote(
            @PathVariable long shelfId,
            @PathVariable long noteId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        long memberId = userPrincipal == null ? 0L : userPrincipal.getMemberId();
        noteService.deleteNote(shelfId, noteId, memberId);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
