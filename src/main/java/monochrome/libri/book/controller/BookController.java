package monochrome.libri.book.controller;

import monochrome.libri.global.exception.ErrorCode;
import monochrome.libri.global.exception.LibriException;
import monochrome.libri.global.response.ApiResponse;
import monochrome.libri.book.dto.request.BookDirectCreateRequestDto;
import monochrome.libri.book.dto.request.BookDirectUpdateRequestDto;
import monochrome.libri.book.dto.response.BookDetailResponseDto;
import monochrome.libri.book.dto.response.BookSliceResponseDto;
import monochrome.libri.book.service.BookService;
import monochrome.libri.search.service.SearchService;
import monochrome.libri.note.dto.response.BookNoteSliceResponseDto;
import monochrome.libri.note.service.NoteService;
import monochrome.libri.global.swagger.ApiErrorCodes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/books")
public class BookController {

    private final BookService bookService;
    private final SearchService searchService;
    private final NoteService noteService;

    public BookController(BookService bookService, SearchService searchService, NoteService noteService) {
        this.bookService = bookService;
        this.searchService = searchService;
        this.noteService = noteService;
    }

    @GetMapping
    @Operation(
            summary = "도서 검색",
            description = "키워드로 도서를 검색하고, 로그인 상태이면 검색어를 최근 검색 기록에 저장합니다."
    )
    @ApiErrorCodes({
            ErrorCode.INVALID_INPUT_VALUE
    })
    public ResponseEntity<ApiResponse<BookSliceResponseDto>> searchBooks(
            @AuthenticationPrincipal monochrome.libri.global.security.UserPrincipal userPrincipal,
            @Parameter(description = "검색어", example = "해리 포터")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "페이지(0부터)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        if (page < 0 || size <= 0) {
            throw new LibriException(ErrorCode.INVALID_INPUT_VALUE);
        }
        Pageable pageable = PageRequest.of(page, size);
        var result = bookService.searchBooks(keyword, pageable);
        long memberId = userPrincipal == null ? 0L : userPrincipal.getMemberId();
        searchService.recordSearch(memberId, keyword);
        BookSliceResponseDto response = new BookSliceResponseDto(
                result.getContent(),
                result.hasNext(),
                result.getNumber(),
                result.getSize()
        );
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{bookId}")
    @Operation(
            summary = "도서 상세 조회",
            description = "도서 상세 정보와 리뷰 통계를 조회합니다. 로그인 상태이면 내 서재 정보가 포함됩니다."
    )
    @ApiErrorCodes({
            ErrorCode.BOOK_NOT_FOUND
    })
    public ResponseEntity<ApiResponse<BookDetailResponseDto>> getBookDetail(
            @PathVariable long bookId,
            @AuthenticationPrincipal monochrome.libri.global.security.UserPrincipal userPrincipal
    ) {
        Long memberId = userPrincipal == null ? null : userPrincipal.getMemberId();
        BookDetailResponseDto response = bookService.getBookDetail(bookId, memberId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{bookId}/notes")
    @Operation(
            summary = "도서 노트 목록 조회",
            description = "도서에 등록된 공개 노트를 페이지네이션으로 조회합니다. 비밀 노트와 차단 관계 회원의 노트는 제외됩니다."
    )
    @ApiErrorCodes({
            ErrorCode.BOOK_NOT_FOUND,
            ErrorCode.INVALID_INPUT_VALUE
    })
    public ResponseEntity<ApiResponse<BookNoteSliceResponseDto>> getNotesByBook(
            @PathVariable long bookId,
            @AuthenticationPrincipal monochrome.libri.global.security.UserPrincipal userPrincipal,
            @Parameter(description = "페이지(0부터)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size
    ) {
        if (page < 0 || size <= 0) {
            throw new LibriException(ErrorCode.INVALID_INPUT_VALUE);
        }
        long memberId = userPrincipal == null ? 0L : userPrincipal.getMemberId();
        Pageable pageable = PageRequest.of(page, size);
        BookNoteSliceResponseDto response = noteService.getPublicNotesByBook(bookId, memberId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/direct")
    @Operation(
            summary = "도서 직접 등록",
            description = "외부 검색 없이 도서를 직접 등록하고 서재에 추가합니다. 요청값 검증 실패 시 필드별 메시지가 반환됩니다."
    )
    @SecurityRequirement(name = "BearerAuth")
    @ApiErrorCodes({
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.MEMBER_NOT_FOUND,
            ErrorCode.INVALID_INPUT_VALUE
    })
    public ResponseEntity<ApiResponse<Void>> createBookDirect(
            @AuthenticationPrincipal monochrome.libri.global.security.UserPrincipal userPrincipal,
            @jakarta.validation.Valid @org.springframework.web.bind.annotation.RequestBody BookDirectCreateRequestDto request
    ) {
        long memberId = userPrincipal == null ? 0L : userPrincipal.getMemberId();
        bookService.createBookDirect(memberId, request);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(ApiResponse.ok());
    }

    @PatchMapping("/{bookId}")
    @Operation(
            summary = "직접 등록한 도서 수정",
            description = "본인이 직접 등록한 도서 정보만 수정할 수 있습니다. 요청값 검증 실패 시 필드별 메시지가 반환됩니다."
    )
    @SecurityRequirement(name = "BearerAuth")
    @ApiErrorCodes({
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.BOOK_NOT_FOUND,
            ErrorCode.ACCESS_DENIED,
            ErrorCode.INVALID_INPUT_VALUE
    })
    public ResponseEntity<ApiResponse<BookDetailResponseDto>> updateBookDirect(
            @PathVariable long bookId,
            @AuthenticationPrincipal monochrome.libri.global.security.UserPrincipal userPrincipal,
            @jakarta.validation.Valid @org.springframework.web.bind.annotation.RequestBody BookDirectUpdateRequestDto request
    ) {
        long memberId = userPrincipal == null ? 0L : userPrincipal.getMemberId();
        BookDetailResponseDto response = bookService.updateBookDirect(bookId, memberId, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
