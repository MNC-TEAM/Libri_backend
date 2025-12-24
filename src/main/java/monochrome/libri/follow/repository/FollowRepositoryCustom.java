package monochrome.libri.follow.repository;

import monochrome.libri.follow.dto.MemberSummaryDto;
import monochrome.libri.member.domain.Member;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface FollowRepositoryCustom {
    Slice<MemberSummaryDto> findFollowers(Member following, Pageable pageable);
    Slice<MemberSummaryDto> findFollowings(Member follower, Pageable pageable);
}
