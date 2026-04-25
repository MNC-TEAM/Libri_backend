package monochrome.libri.member.dto.response;

import monochrome.libri.member.domain.Member;

public record MemberProfileResponseDto(
        long memberId,
        String username,
        String nickname,
        String profilePath,
        boolean privateAccount,
        long followerCount,
        long followingCount,
        boolean mine,
        boolean following
) {
    public static MemberProfileResponseDto from(
            Member member,
            long followerCount,
            long followingCount,
            boolean mine,
            boolean following
    ) {
        return new MemberProfileResponseDto(
                member.getId(),
                member.getUsername(),
                member.getNickname(),
                member.getProfilePath(),
                member.isPrivateAccount(),
                followerCount,
                followingCount,
                mine,
                following
        );
    }
}
