package monochrome.libri.review.controller;

import monochrome.libri.global.exception.ErrorCode;
import monochrome.libri.global.response.ApiResponse;
import monochrome.libri.global.security.UserPrincipal;
import monochrome.libri.global.swagger.ApiErrorCodes;
import monochrome.libri.review.service.ReviewBookmarkService;
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
@RequestMapping("/api/v1/reviews/{reviewId}/bookmarks")
@SecurityRequirement(name = "BearerAuth")
public class ReviewBookmarkController {

    private final ReviewBookmarkService reviewBookmarkService;

    public ReviewBookmarkController(ReviewBookmarkService reviewBookmarkService) {
        this.reviewBookmarkService = reviewBookmarkService;
    }

    @PostMapping
    @Operation(
            summary = "리뷰 북마크",
            description = "리뷰를 북마크에 추가합니다."
    )
    @ApiErrorCodes({
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.MEMBER_NOT_FOUND,
            ErrorCode.REVIEW_NOT_FOUND
    })
    public ResponseEntity<ApiResponse<Void>> bookmark(
            @PathVariable long reviewId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        long memberId = userPrincipal == null ? 0L : userPrincipal.getMemberId();
        reviewBookmarkService.bookmark(reviewId, memberId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok());
    }

    @DeleteMapping
    @Operation(
            summary = "리뷰 북마크 취소",
            description = "리뷰 북마크를 해제합니다."
    )
    @ApiErrorCodes({
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.REVIEW_NOT_FOUND
    })
    public ResponseEntity<ApiResponse<Void>> unbookmark(
            @PathVariable long reviewId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        long memberId = userPrincipal == null ? 0L : userPrincipal.getMemberId();
        reviewBookmarkService.unbookmark(reviewId, memberId);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
