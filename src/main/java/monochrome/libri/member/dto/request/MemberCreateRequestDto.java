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
public record MemberCreateRequestDto(
        @NotNull
        SignType provider,
        @NotBlank
        String providerUserId,
        @Email
        String primaryEmail,                // 일반 로그인일 경우 필수로 사용할 이메일
        @Email
        String emailFromProvider,           // 소셜 로그인에서 내려온 이메일(없으면 공백)
        Boolean emailVerifiedFromProvider,  // 클라이언트가 true로 보내도 서버에서 검증 후 확정하는 것을 권장
        @NotBlank @Size(min = 3, max = 30)
        String username,
        @Size(min = 2, max = 30)
        String nickname,
        @Size(min = 8, max = 100)
        String rawPassword,                 // 해시가 아닌 원문 패스워드 전달 시 이름을 rawPassword로 명확화
        String profilePath,
        Role role                           // 기본 ROLE_USER 권장
) {
}
