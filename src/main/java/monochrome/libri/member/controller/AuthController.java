package monochrome.libri.member.controller;

import jakarta.validation.Valid;
import monochrome.libri.global.exception.ErrorCode;
import monochrome.libri.global.response.ApiResponse;
import monochrome.libri.global.security.token.JwtTokenService;
import monochrome.libri.global.swagger.ApiErrorCodes;
import monochrome.libri.member.dto.request.EmailLoginRequestDto;
import monochrome.libri.member.dto.request.EmailSignUpRequestDto;
import monochrome.libri.member.dto.request.RefreshTokenRequestDto;
import monochrome.libri.member.dto.request.SocialSignUpRequestDto;
import monochrome.libri.member.dto.response.LoginResponseDto;
import monochrome.libri.member.dto.response.MemberResponseDto;
import monochrome.libri.member.dto.response.TokenRefreshResponseDto;
import monochrome.libri.member.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtTokenService jwtTokenService;

    public AuthController(AuthService authService, JwtTokenService jwtTokenService) {
        this.authService = authService;
        this.jwtTokenService = jwtTokenService;
    }

    /**
     * 이메일 회원가입
     * POST /api/v1/auth/signup/email
     */
    @ApiErrorCodes({
            ErrorCode.EMAIL_ALREADY_EXISTS,
    })
    @PostMapping("/signup/email")
    @Operation(
            summary = "이메일 회원가입",
            description = "이메일과 비밀번호로 회원을 생성합니다."
    )
    public ResponseEntity<ApiResponse<Void>> signupByEmail(
            @Valid @RequestBody EmailSignUpRequestDto request
    ) {
        authService.signupByEmail(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok());
    }

    /**
     * 이메일 로그인
     * POST /api/v1/auth/login/email
     */
    @ApiErrorCodes({
            ErrorCode.INVALID_LOGIN
    })
    @PostMapping("/login/email")
    @Operation(
            summary = "이메일 로그인",
            description = "이메일 로그인 후 액세스 토큰과 리프레시 토큰을 발급합니다."
    )
    public ResponseEntity<ApiResponse<LoginResponseDto>> loginByEmail(
            @Valid @RequestBody EmailLoginRequestDto request
    ) {
        MemberResponseDto memberResponse = authService.loginByEmail(request);
        var tokenPair = jwtTokenService.issueTokenPair(memberResponse.id());

        LoginResponseDto response = LoginResponseDto.of(
                tokenPair.tokenType(),
                tokenPair.accessToken(),
                tokenPair.refreshToken(),
                memberResponse
        );

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * 액세스 토큰 재발급
     * POST /api/v1/auth/refresh
     */
    @ApiErrorCodes({
            ErrorCode.INVALID_REFRESH_TOKEN,
            ErrorCode.TOKEN_TYPE_MISMATCH
    })
    @PostMapping("/refresh")
    @Operation(
            summary = "액세스 토큰 재발급",
            description = "리프레시 토큰으로 액세스 토큰과 리프레시 토큰을 재발급합니다."
    )
    public ResponseEntity<ApiResponse<TokenRefreshResponseDto>> refresh(
            @Valid @RequestBody RefreshTokenRequestDto request
    ) {
        authService.validateRefreshRequest(request);
        var tokenPair = jwtTokenService.refresh(request.refreshToken());
        return ResponseEntity.ok(ApiResponse.ok(TokenRefreshResponseDto.from(tokenPair)));
    }

    /**
     * 로그아웃
     * POST /api/v1/auth/logout
     */
    @ApiErrorCodes({
            ErrorCode.INVALID_REFRESH_TOKEN,
            ErrorCode.TOKEN_TYPE_MISMATCH
    })
    @PostMapping("/logout")
    @Operation(
            summary = "로그아웃",
            description = "리프레시 토큰을 무효화하고 로그아웃을 처리합니다."
    )
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody RefreshTokenRequestDto request
    ) {
        authService.logout(request.refreshToken());
        jwtTokenService.revokeRefreshToken(request.refreshToken());
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
