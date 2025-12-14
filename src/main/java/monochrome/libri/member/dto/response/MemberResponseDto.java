package monochrome.libri.member.dto.response;

import monochrome.libri.member.domain.Member;
import monochrome.libri.member.domain.Role;
import monochrome.libri.member.domain.SignType;
import monochrome.libri.member.domain.Status;

public record MemberResponseDto(
    long id,
    SignType provider,
    String providerUserId,
    String primaryEmail,
    boolean emailVerified,
    String emailFromProvider,
    Boolean emailVerifiedFromProvider,
    String username,
    String nickname,
    String profilePath,
    Status status,
    Role role
) {
    public static MemberResponseDto from(Member member) {
        return new MemberResponseDto(
                member.getId(),
                member.getProvider(),
                member.getProviderUserId(),
                member.getPrimaryEmail(),
                member.isEmailVerified(),
                member.getEmailFromProvider(),
                member.getEmailVerifiedFromProvider(),
                member.getUsername(),
                member.getNickname(),
                member.getProfilePath(),
                member.getStatus(),
                member.getRole()
        );
    }
}
