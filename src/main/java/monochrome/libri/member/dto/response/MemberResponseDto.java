package monochrome.libri.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import monochrome.libri.member.domain.Member;
import monochrome.libri.member.domain.SignType;

public record MemberResponseDto(
    @Schema(description = "회원 ID", example = "12")
    long id,
    @Schema(
            description = "회원의 기본 가입 타입. 자동 연동된 경우 EMAIL일 수 있음",
            example = "EMAIL"
    )
    SignType provider,
    @Schema(description = "회원 이메일. 소셜 provider가 이메일을 주지 않으면 null일 수 있음", example = "user@test.com")
    String email,
    @Schema(description = "닉네임", example = "nick12")
    String nickname,
    @Schema(description = "프로필 이미지 경로", example = "/profile/12")
    String profilePath,
    @Schema(description = "비공개 계정 여부", example = "false")
    boolean privateAccount,
    @Schema(description = "팔로워 수", example = "0")
    long followerCount,
    @Schema(description = "팔로잉 수", example = "0")
    long followingCount
) {
    public static MemberResponseDto from(Member member) {
        return from(member, 0L, 0L);
    }

    public static MemberResponseDto from(Member member, long followerCount, long followingCount) {
        return new MemberResponseDto(
                member.getId(),
                member.getProvider(),
                member.getEmail(),
                member.getNickname(),
                member.getProfilePath(),
                member.isPrivateAccount(),
                followerCount,
                followingCount
        );
    }
}
