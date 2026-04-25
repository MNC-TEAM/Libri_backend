package monochrome.libri.member.dto.response;

import monochrome.libri.member.domain.Member;
import monochrome.libri.member.domain.MemberStatus;
import monochrome.libri.member.domain.Role;
import monochrome.libri.member.domain.SignType;

public record MemberResponseDto(
    long id,
    SignType provider,
    String email,
    String nickname,
    String profilePath,
    boolean privateAccount,
    long followerCount,
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
