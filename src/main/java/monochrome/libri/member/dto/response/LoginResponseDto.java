package monochrome.libri.member.dto.response;

public record LoginResponseDto(
        String tokenType,
        String accessToken,
        String refreshToken,
        MemberResponseDto memberResponseDto
) {
    public static LoginResponseDto of(
            String tokenType,
            String accessToken,
            String refreshToken,
            MemberResponseDto memberResponseDto
    ) {
        return new LoginResponseDto(tokenType, accessToken, refreshToken, memberResponseDto);
    }
}
