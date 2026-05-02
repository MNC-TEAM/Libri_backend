package monochrome.libri.block.dto.response;

import monochrome.libri.block.domain.MemberBlock;

import java.time.LocalDateTime;

public record BlockedMemberItemResponseDto(
        long memberId,
        String username,
        String nickname,
        String profilePath,
        LocalDateTime blockedAt
) {
    public static BlockedMemberItemResponseDto from(MemberBlock memberBlock) {
        return new BlockedMemberItemResponseDto(
                memberBlock.getBlocked().getId(),
                memberBlock.getBlocked().getUsername(),
                memberBlock.getBlocked().getNickname(),
                memberBlock.getBlocked().getProfilePath(),
                memberBlock.getCreatedDate()
        );
    }
}
