package monochrome.libri.review.controller;

import jakarta.validation.Valid;
import monochrome.libri.global.exception.ErrorCode;
import monochrome.libri.global.exception.LibriException;
import monochrome.libri.global.response.ApiResponse;
import monochrome.libri.global.security.UserPrincipal;
import monochrome.libri.global.swagger.ApiErrorCodes;
import monochrome.libri.review.dto.request.ReviewCreateRequestDto;
import monochrome.libri.review.dto.request.ReviewUpdateRequestDto;
import monochrome.libri.review.dto.response.ReviewSliceResponseDto;
import monochrome.libri.review.dto.response.ReviewSliceWithStatsResponseDto;
import monochrome.libri.review.dto.response.ReviewSummaryResponseDto;
import monochrome.libri.review.service.ReviewService;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class ReviewController {

    private static final int DEFAULT_PAGE_SIZE = 10;

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/books/{bookId}/reviews")
    @Operation(
            summary = "도서 리뷰 목록 조회",
            description = "도서에 등록된 리뷰를 페이지네이션과 함께 조회하고 평점 통계를 포함합니다."
    )
    @ApiErrorCodes({
            ErrorCode.BOOK_NOT_FOUND,
            ErrorCode.PAGINATION_INVALID
    })
    public ResponseEntity<ApiResponse<ReviewSliceWithStatsResponseDto>> getReviewsByBook(
            @PathVariable long bookId,
            @Parameter(description = "페이지(0부터)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "" + DEFAULT_PAGE_SIZE) int size
    ) {
        if (page < 0 || size <= 0) {
            throw new LibriException(ErrorCode.PAGINATION_INVALID);
        }
        Pageable pageable = PageRequest.of(page, size);
        ReviewSliceWithStatsResponseDto response = reviewService.getReviewsByBook(bookId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/reviews")
    @Operation(
            summary = "리뷰 작성",
            description = "도서에 대한 리뷰를 작성합니다."
    )
    @SecurityRequirement(name = "BearerAuth")
    @ApiErrorCodes({
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.MEMBER_NOT_FOUND,
            ErrorCode.BOOK_NOT_FOUND,
            ErrorCode.INVALID_INPUT_VALUE
    })
    public ResponseEntity<ApiResponse<Void>> createReview(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody ReviewCreateRequestDto request
    ) {
        long memberId = userPrincipal == null ? 0L : userPrincipal.getMemberId();
        reviewService.createReview(memberId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok());
    }

    @PatchMapping("/reviews/{reviewId}")
    @Operation(
            summary = "리뷰 수정",
            description = "본인이 작성한 리뷰만 수정할 수 있습니다."
    )
    @SecurityRequirement(name = "BearerAuth")
    @ApiErrorCodes({
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.REVIEW_NOT_FOUND,
            ErrorCode.ACCESS_DENIED,
            ErrorCode.INVALID_INPUT_VALUE
    })
    public ResponseEntity<ApiResponse<ReviewSummaryResponseDto>> updateReview(
            @PathVariable long reviewId,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody ReviewUpdateRequestDto request
    ) {
        long memberId = userPrincipal == null ? 0L : userPrincipal.getMemberId();
        ReviewSummaryResponseDto response = reviewService.updateReview(reviewId, memberId, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/reviews/{reviewId}")
    @Operation(
            summary = "리뷰 삭제",
            description = "본인이 작성한 리뷰를 삭제합니다."
    )
    @SecurityRequirement(name = "BearerAuth")
    @ApiErrorCodes({
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.REVIEW_NOT_FOUND,
            ErrorCode.ACCESS_DENIED
    })
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @PathVariable long reviewId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        long memberId = userPrincipal == null ? 0L : userPrincipal.getMemberId();
        reviewService.deleteReview(reviewId, memberId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @GetMapping("/reviews/me")
    @Operation(
            summary = "내 리뷰 목록 조회",
            description = "회원이 작성한 리뷰 목록을 페이지네이션으로 조회합니다."
    )
    @SecurityRequirement(name = "BearerAuth")
    @ApiErrorCodes({
            ErrorCode.AUTHENTICATION_FAILED,
            ErrorCode.PAGINATION_INVALID
    })
    public ResponseEntity<ApiResponse<ReviewSliceResponseDto>> getMyReviews(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "페이지(0부터)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "" + DEFAULT_PAGE_SIZE) int size
    ) {
        if (page < 0 || size <= 0) {
            throw new LibriException(ErrorCode.PAGINATION_INVALID);
        }
        long memberId = userPrincipal == null ? 0L : userPrincipal.getMemberId();
        Pageable pageable = PageRequest.of(page, size);
        ReviewSliceResponseDto response = reviewService.getReviewsByMember(memberId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
