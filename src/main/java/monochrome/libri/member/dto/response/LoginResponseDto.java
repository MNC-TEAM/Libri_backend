package monochrome.libri.member.dto.response;

public record LoginResponseDto(
        String tokenType,
        String accessToken,
        MemberResponseDto memberResponseDto
) {
    public static LoginResponseDto of(String tokenType, String accessToken, MemberResponseDto memberResponseDto) {
        return new LoginResponseDto(tokenType, accessToken, memberResponseDto);
    }
}
