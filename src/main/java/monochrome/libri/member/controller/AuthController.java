package monochrome.libri.member.controller;

import jakarta.validation.Valid;
import monochrome.libri.global.exception.ErrorCode;
import monochrome.libri.global.exception.LibriException;
import monochrome.libri.global.response.ApiResponse;
import monochrome.libri.global.security.token.JwtTokenService;
import monochrome.libri.global.swagger.ApiErrorCodes;
import monochrome.libri.member.config.SocialLoginProperties;
import monochrome.libri.member.domain.SignType;
import monochrome.libri.member.dto.request.EmailLoginRequestDto;
import monochrome.libri.member.dto.request.EmailSignUpRequestDto;
import monochrome.libri.member.dto.request.RefreshTokenRequestDto;
import monochrome.libri.member.dto.request.SocialLoginRequestDto;
import monochrome.libri.member.dto.response.LoginResponseDto;
import monochrome.libri.member.dto.response.LoginApiResponseDoc;
import monochrome.libri.member.dto.response.MemberResponseDto;
import monochrome.libri.member.dto.response.TokenRefreshResponseDto;
import monochrome.libri.member.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtTokenService jwtTokenService;
    private final SocialLoginProperties socialLoginProperties;

    public AuthController(
            AuthService authService,
            JwtTokenService jwtTokenService,
            SocialLoginProperties socialLoginProperties
    ) {
        this.authService = authService;
        this.jwtTokenService = jwtTokenService;
        this.socialLoginProperties = socialLoginProperties;
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
        return ResponseEntity.ok(ApiResponse.ok(issueLoginResponse(authService.loginByEmail(request))));
    }

    @ApiErrorCodes({
            ErrorCode.INVALID_SOCIAL_TOKEN,
            ErrorCode.INVALID_INPUT_VALUE
    })
    @PostMapping("/login/social")
    @Operation(
            summary = "소셜 로그인",
            description = """
                    소셜 로그인 후 access/refresh token을 발급합니다.

                    요청 규칙:
                    - provider=KAKAO  → accessToken 또는 code(인가 코드) 중 하나 필수
                    - provider=APPLE  → idToken 필수
                    - provider=GOOGLE → idToken(Google Sign-In SDK에서 받은 ID Token) 필수

                    동작 방식:
                    - 이미 연동된 소셜 계정이면 해당 회원으로 로그인
                    - 같은 이메일의 기존 회원이 있으면 자동 연동 후 로그인
                    - 일치 회원이 없으면 새 회원을 생성한 뒤 로그인
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "소셜 로그인 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginApiResponseDoc.class),
                            examples = {
                                    @ExampleObject(
                                            name = "KakaoSocialLoginSuccess",
                                            summary = "카카오 로그인 성공",
                                            value = """
                                                    {
                                                      "success": true,
                                                      "code": "OK",
                                                      "message": null,
                                                      "data": {
                                                        "tokenType": "Bearer",
                                                        "accessToken": "ACCESS_TOKEN",
                                                        "refreshToken": "REFRESH_TOKEN",
                                                        "memberResponseDto": {
                                                          "id": 12,
                                                          "provider": "EMAIL",
                                                          "email": "user@test.com",
                                                          "nickname": "nick12",
                                                          "profilePath": "/profile/12",
                                                          "privateAccount": false,
                                                          "followerCount": 0,
                                                          "followingCount": 0
                                                        }
                                                      }
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "AppleSocialLoginSuccess",
                                            summary = "애플 로그인 성공",
                                            value = """
                                                    {
                                                      "success": true,
                                                      "code": "OK",
                                                      "message": null,
                                                      "data": {
                                                        "tokenType": "Bearer",
                                                        "accessToken": "ACCESS_TOKEN",
                                                        "refreshToken": "REFRESH_TOKEN",
                                                        "memberResponseDto": {
                                                          "id": 34,
                                                          "provider": "APPLE",
                                                          "email": null,
                                                          "nickname": "apple_000123",
                                                          "profilePath": null,
                                                          "privateAccount": false,
                                                          "followerCount": 0,
                                                          "followingCount": 0
                                                        }
                                                      }
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "MissingKakaoAuthorizationCode",
                                            summary = "카카오 인가 코드 누락",
                                            value = """
                                                    {
                                                      "success": false,
                                                      "code": "C002",
                                                      "message": "잘못된 요청입니다.",
                                                      "data": null
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "UnsupportedProvider",
                                            summary = "지원하지 않는 provider",
                                            value = """
                                                    {
                                                      "success": false,
                                                      "code": "C002",
                                                      "message": "잘못된 요청입니다.",
                                                      "data": null
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "소셜 토큰 검증 실패 또는 계정 연동 충돌",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "InvalidSocialToken",
                                            summary = "카카오/애플 토큰 검증 실패",
                                            value = """
                                                    {
                                                      "success": false,
                                                      "code": "A005",
                                                      "message": "유효하지 않은 소셜 로그인 토큰입니다.",
                                                      "data": null
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "AuthenticationConflict",
                                            summary = "이미 다른 회원에 연동된 소셜 계정",
                                            value = """
                                                    {
                                                      "success": false,
                                                      "code": "A001",
                                                      "message": "인증에 실패했습니다.",
                                                      "data": null
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "WithdrawnMemberSocialLogin",
                                            summary = "탈퇴 회원 로그인 시도",
                                            value = """
                                                    {
                                                      "success": false,
                                                      "code": "M003",
                                                      "message": "이메일 또는 비밀번호가 올바르지 않습니다.",
                                                      "data": null
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
    public ResponseEntity<ApiResponse<LoginResponseDto>> loginBySocial(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = """
                            카카오 access token 예시:
                            {
                              "provider": "KAKAO",
                              "accessToken": "kakao-access-token"
                            }

                            카카오 authorization code 예시:
                            {
                              "provider": "KAKAO",
                              "code": "kakao-authorization-code"
                            }
                            
                            애플 예시:
                            {
                              "provider": "APPLE",
                              "idToken": "apple-id-token"
                            }
                            """
            )
            @Valid @RequestBody SocialLoginRequestDto request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(issueLoginResponse(authService.loginBySocial(request))));
    }

    @GetMapping("/login/kakao")
    @Operation(
            summary = "카카오 로그인 시작",
            description = "카카오 인증 페이지로 리다이렉트합니다."
    )
    public ResponseEntity<Void> redirectToKakaoLogin() {
        URI authorizationUri = UriComponentsBuilder
                .fromUriString(socialLoginProperties.kakao().authBaseUrl())
                .path("/oauth/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", socialLoginProperties.kakao().clientId())
                .queryParam("redirect_uri", socialLoginProperties.kakao().redirectUri())
                .build(true)
                .toUri();

        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, authorizationUri.toString())
                .build();
    }

    @ApiErrorCodes({
            ErrorCode.INVALID_SOCIAL_TOKEN,
            ErrorCode.INVALID_INPUT_VALUE
    })
    @GetMapping("/login/kakao/callback")
    @Operation(
            summary = "카카오 로그인 콜백",
            description = "카카오 인가 코드를 받아 로그인 또는 회원가입을 처리합니다."
    )
    public ResponseEntity<ApiResponse<Void>> loginByKakaoCallback(
            @RequestParam(required = false) String code
    ) {
        if (code == null || code.isBlank()) {
            throw new LibriException(ErrorCode.INVALID_INPUT_VALUE);
        }

        SocialLoginRequestDto request = new SocialLoginRequestDto(SignType.KAKAO, null, null, code);
        authService.loginBySocial(request);
        return ResponseEntity.ok(ApiResponse.ok());
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

    private LoginResponseDto issueLoginResponse(MemberResponseDto memberResponse) {
        var tokenPair = jwtTokenService.issueTokenPair(memberResponse.id());
        return LoginResponseDto.of(
                tokenPair.tokenType(),
                tokenPair.accessToken(),
                tokenPair.refreshToken(),
                memberResponse
        );
    }
}
