package monochrome.libri.follow.service;

import monochrome.libri.follow.domain.Follow;
import monochrome.libri.follow.domain.FollowStatus;
import monochrome.libri.follow.dto.MemberSummaryDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface FollowService {
    void follow(Long followerMemberId, Long followingMemberId);
    void unfollow(Long followerMemberId, Long followingMemberId);
    Slice<MemberSummaryDto> findFollowers(Long memberId, Pageable pageable);
    Slice<MemberSummaryDto> findFollowings(Long memberId, Pageable pageable);
}
