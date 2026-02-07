package monochrome.libri.home.controller;

import monochrome.libri.global.exception.ErrorCode;
import monochrome.libri.global.response.ApiResponse;
import monochrome.libri.global.security.UserPrincipal;
import monochrome.libri.global.swagger.ApiErrorCodes;
import monochrome.libri.home.dto.response.HomeResponseDto;
import monochrome.libri.home.service.HomeService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/home")
public class HomeController {

    private final HomeService homeService;

    public HomeController(HomeService homeService) {
        this.homeService = homeService;
    }

    @GetMapping
    @Operation(
            summary = "홈 화면 데이터 조회",
            description = "회원이면 요약/책장/진행중/읽고싶음/완독 목록을 포함하고, 비회원이면 기본값을 반환합니다."
    )
    @ApiErrorCodes({
            ErrorCode.MEMBER_NOT_FOUND
    })
    public ResponseEntity<ApiResponse<HomeResponseDto>> getHome(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long memberId = userPrincipal == null ? null : userPrincipal.getMemberId();
        HomeResponseDto response = homeService.getHome(memberId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
