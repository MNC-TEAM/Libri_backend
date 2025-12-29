package monochrome.libri.member.dto.response;

public record LoginResponseDto(
        String accessToken,
        MemberResponseDto memberResponseDto
) {
    public static LoginResponseDto of(String accessToken, MemberResponseDto memberResponseDto) {
        return new LoginResponseDto(accessToken, memberResponseDto);
    }
}
