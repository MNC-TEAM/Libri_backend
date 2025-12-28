package monochrome.libri.member.dto.response;

import monochrome.libri.member.domain.Member;
import monochrome.libri.member.domain.SignType;

public record SignUpResponseDto(
        long id,
        SignType provider,
        String email,
        boolean emailVerified,
        String nickname,
        String profilePath
) {
    public static SignUpResponseDto from(Member member) {
        return new SignUpResponseDto(
                member.getId(),
                member.getProvider(),
                member.getEmail(),
                member.isEmailVerified(),
                member.getNickname(),
                member.getProfilePath()
        );
    }
}
