package monochrome.libri.block.dto.response;

import java.util.List;

public record BlockedMemberListResponseDto(
        long totalCount,
        List<BlockedMemberItemResponseDto> content,
        boolean hasNext,
        int page,
        int size
) {
    public static BlockedMemberListResponseDto empty(int page, int size) {
        return new BlockedMemberListResponseDto(0L, List.of(), false, page, size);
    }
}
