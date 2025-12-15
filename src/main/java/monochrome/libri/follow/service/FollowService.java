package monochrome.libri.follow.service;

import monochrome.libri.follow.domain.Follow;
import monochrome.libri.follow.domain.Status;
import monochrome.libri.follow.dto.MemberSummaryDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface FollowService {
    Follow create(Long followerMemberId, Long followingMemberId);
    Follow get(Long followId);

    Slice<MemberSummaryDto> getFollowers(Long memberId, Pageable pageable);
    Slice<MemberSummaryDto> getFollowings(Long memberId, Pageable pageable);

    Follow updateStatus(Long followId, Status status);
    void delete(Long followId);
}
