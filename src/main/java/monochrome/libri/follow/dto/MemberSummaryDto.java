package monochrome.libri.follow.dto;

import monochrome.libri.member.domain.Member;

public record MemberSummaryDto(
        Long memberId,
        String username,
        String nickname,
        String profilePath
) {
    public static MemberSummaryDto from(Member member) {
        return new MemberSummaryDto(
                member.getId(),
                member.getUsername(),
                member.getNickname(),
                member.getProfilePath()
        );
    }
}
