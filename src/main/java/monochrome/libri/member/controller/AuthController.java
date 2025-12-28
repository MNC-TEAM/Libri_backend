package monochrome.libri.member.controller;

import jakarta.validation.Valid;
import monochrome.libri.global.response.ApiResponse;
import monochrome.libri.member.dto.request.EmailLoginRequestDto;
import monochrome.libri.member.dto.request.EmailSignUpRequestDto;
import monochrome.libri.member.dto.request.SocialSignUpRequestDto;
import monochrome.libri.member.dto.response.MemberResponseDto;
import monochrome.libri.member.dto.response.SignUpResponseDto;
import monochrome.libri.member.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 이메일 회원가입
     * POST /api/v1/auth/signup/email
     */
    @PostMapping("/signup/email")
    public ResponseEntity<ApiResponse<SignUpResponseDto>> signupByEmail(
            @Valid @RequestBody EmailSignUpRequestDto request
    ) {
        SignUpResponseDto response = authService.signupByEmail(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * 이메일 로그인
     * POST /api/v1/auth/login/email
     */
    @PostMapping("/signin/email")
    public ResponseEntity<ApiResponse<MemberResponseDto>> loginByEmail(
            @Valid @RequestBody EmailLoginRequestDto request
    ) {
        MemberResponseDto response = authService.loginByEmail(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * 로그아웃
     * POST /api/v1/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        authService.logout();
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
