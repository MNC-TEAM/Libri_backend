package monochrome.libri.member.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import monochrome.libri.member.domain.Role;
import monochrome.libri.member.domain.SignType;

/**
 * 회원 생성 요청 DTO (record)
 * @param provider
 * @param providerUserId
 * @param primaryEmail
 * @param emailFromProvider
 * @param emailVerifiedFromProvider
 * @param username
 * @param nickname
 * @param rawPassword
 * @param profilePath
 * @param role
 */
public record SocialSignUpRequestDto(
        @NotNull(message = "가입 방식은 필수입니다.")
        SignType provider,
        @NotBlank(message = "소셜 사용자 식별값은 필수입니다.")
        String providerUserId,
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String primaryEmail,                // 일반 로그인일 경우 필수로 사용할 이메일
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String emailFromProvider,           // 소셜 로그인에서 내려온 이메일(없으면 공백)
        Boolean emailVerifiedFromProvider,  // 클라이언트가 true로 보내도 서버에서 검증 후 확정하는 것을 권장
        @NotBlank(message = "아이디는 필수입니다.")
        @Size(min = 3, max = 30, message = "아이디는 3자 이상 30자 이하여야 합니다.")
        String username,
        @Size(min = 2, max = 30, message = "닉네임은 2자 이상 30자 이하여야 합니다.")
        String nickname,
        @Size(min = 8, max = 100, message = "비밀번호는 8자 이상 100자 이하여야 합니다.")
        String rawPassword,                 // 해시가 아닌 원문 패스워드 전달 시 이름을 rawPassword로 명확화
        String profilePath,
        Role role                           // 기본 ROLE_USER 권장
) {
}
