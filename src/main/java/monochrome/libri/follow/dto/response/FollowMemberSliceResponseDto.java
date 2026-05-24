package monochrome.libri.follow.dto.response;

import monochrome.libri.follow.dto.MemberSummaryDto;

import java.util.List;

public record FollowMemberSliceResponseDto(
        List<MemberSummaryDto> content,
        boolean hasNext,
        int page,
        int size
) {
}
