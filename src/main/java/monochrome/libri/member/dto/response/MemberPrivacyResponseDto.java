package monochrome.libri.member.dto.response;

public record MemberPrivacyResponseDto(
        long memberId,
        boolean privateAccount
) {
}
