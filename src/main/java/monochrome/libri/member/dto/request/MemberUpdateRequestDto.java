package monochrome.libri.member.dto.request;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import monochrome.libri.member.domain.Role;
import monochrome.libri.member.domain.Status;

public record MemberUpdateRequestDto(
    @Nullable @Email
    String primaryEmail,
    @Nullable
    Boolean emailVerified,
    @Nullable @Email
    String emailFromProvider,              // 소셜 원천 이메일/검증 여부가 사후 동기화로 들어올 수 있어 열어둠
    @Nullable
    Boolean emailVerifiedFromProvider,
    @Nullable @Size(min = 3, max = 30)
    String username,
    @Nullable @Size(min = 1, max = 30)
    String nickname,
    @Nullable @Size(min = 8, max = 100)
    String rawPassword,                     // 비밀번호 변경 시에만 전달 (원문 → 서버에서 해시)
    @Nullable String profilePath,
    @Nullable Status status,
    @Nullable Role role
) {
}
