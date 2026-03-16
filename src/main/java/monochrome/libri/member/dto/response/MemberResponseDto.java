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
    boolean privateAccount
) {
    public static MemberResponseDto from(Member member) {
        return new MemberResponseDto(
                member.getId(),
                member.getProvider(),
                member.getEmail(),
                member.getNickname(),
                member.getProfilePath(),
                member.isPrivateAccount()
        );
    }
}
