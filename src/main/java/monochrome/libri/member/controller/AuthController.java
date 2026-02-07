package monochrome.libri.member.controller;

import jakarta.validation.Valid;
import monochrome.libri.global.exception.ErrorCode;
import monochrome.libri.global.response.ApiResponse;
import monochrome.libri.global.security.token.JwtTokenService;
import monochrome.libri.global.swagger.ApiErrorCodes;
import monochrome.libri.member.dto.request.EmailLoginRequestDto;
import monochrome.libri.member.dto.request.EmailSignUpRequestDto;
import monochrome.libri.member.dto.request.SocialSignUpRequestDto;
import monochrome.libri.member.dto.response.LoginResponseDto;
import monochrome.libri.member.dto.response.MemberResponseDto;
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
            description = "이메일 로그인 후 액세스 토큰을 발급합니다."
    )
    public ResponseEntity<ApiResponse<LoginResponseDto>> loginByEmail(
            @Valid @RequestBody EmailLoginRequestDto request
    ) {
        MemberResponseDto memberResponse = authService.loginByEmail(request);
        String token = jwtTokenService.issueAccessToken(memberResponse.id());

        LoginResponseDto response = LoginResponseDto.of("Bearer", token, memberResponse);

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * 로그아웃
     * POST /api/v1/auth/logout
     */
    @PostMapping("/logout")
    @Operation(
            summary = "로그아웃",
            description = "로그아웃을 처리합니다. (현재 토큰 무효화 로직은 미구현)"
    )
    public ResponseEntity<ApiResponse<Void>> logout() {
        authService.logout();
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
