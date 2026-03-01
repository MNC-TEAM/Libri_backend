package monochrome.libri.search.controller;

import monochrome.libri.global.exception.ErrorCode;
import monochrome.libri.global.exception.LibriException;
import monochrome.libri.global.response.ApiResponse;
import monochrome.libri.global.security.UserPrincipal;
import monochrome.libri.global.swagger.ApiErrorCodes;
import monochrome.libri.search.dto.response.RecentSearchListResponseDto;
import monochrome.libri.search.dto.response.TrendingKeywordListResponseDto;
import monochrome.libri.search.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/searches")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/recent")
    @Operation(
            summary = "최근 검색어 조회",
            description = "회원의 최근 검색어 목록을 페이지네이션으로 조회합니다."
    )
    @SecurityRequirement(name = "BearerAuth")
    @ApiErrorCodes({
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.PAGINATION_INVALID
    })
    public ResponseEntity<ApiResponse<RecentSearchListResponseDto>> getRecentSearches(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "페이지(0부터)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        if (page < 0 || size <= 0) {
            throw new LibriException(ErrorCode.PAGINATION_INVALID);
        }
        long memberId = userPrincipal == null ? 0L : userPrincipal.getMemberId();
        Pageable pageable = PageRequest.of(page, size);
        RecentSearchListResponseDto response = searchService.getRecentSearches(memberId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/recent")
    @Operation(
            summary = "최근 검색어 전체 삭제",
            description = "회원의 최근 검색어 기록을 모두 삭제합니다."
    )
    @SecurityRequirement(name = "BearerAuth")
    @ApiErrorCodes({
            ErrorCode.AUTHENTICATION_FAILED
    })
    public ResponseEntity<ApiResponse<Void>> deleteAllRecentSearches(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        long memberId = userPrincipal == null ? 0L : userPrincipal.getMemberId();
        searchService.deleteAllRecentSearches(memberId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @DeleteMapping("/recent/{searchId}")
    @Operation(
            summary = "최근 검색어 삭제",
            description = "특정 최근 검색어 항목을 삭제합니다."
    )
    @SecurityRequirement(name = "BearerAuth")
    @ApiErrorCodes({
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.INVALID_INPUT_VALUE
    })
    public ResponseEntity<ApiResponse<Void>> deleteRecentSearch(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable long searchId
    ) {
        long memberId = userPrincipal == null ? 0L : userPrincipal.getMemberId();
        searchService.deleteRecentSearch(memberId, searchId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @GetMapping("/trending")
    @Operation(
            summary = "인기 검색어 조회",
            description = "전체 인기 검색어 목록을 조회합니다."
    )
    public ResponseEntity<ApiResponse<TrendingKeywordListResponseDto>> getTrending(
            @Parameter(description = "조회 개수(1~20)", example = "10")
            @RequestParam(defaultValue = "10") int limit
    ) {
        TrendingKeywordListResponseDto response = searchService.getTrendingKeywords(limit);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
