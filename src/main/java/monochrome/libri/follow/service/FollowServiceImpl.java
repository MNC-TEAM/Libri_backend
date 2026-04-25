package monochrome.libri.follow.service;

import monochrome.libri.block.service.BlockService;
import lombok.extern.slf4j.Slf4j;
import monochrome.libri.follow.domain.Follow;
import monochrome.libri.follow.domain.FollowStatus;
import monochrome.libri.follow.dto.MemberSummaryDto;
import monochrome.libri.follow.repository.FollowRepository;
import monochrome.libri.global.exception.ErrorCode;
import monochrome.libri.global.exception.LibriException;
import monochrome.libri.member.domain.Member;
import monochrome.libri.member.service.MemberService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
public class FollowServiceImpl implements FollowService{

    private final FollowRepository followRepository;
    private final MemberService memberService;
    private final BlockService blockService;

    public FollowServiceImpl(FollowRepository followRepository, MemberService memberService, BlockService blockService) {
        this.followRepository = followRepository;
        this.memberService = memberService;
        this.blockService = blockService;
    }

    // ************* 내부 메서드 *************
    private void validateNotSelf(Long followerMemberId, Long followingMemberId) {
        if(followerMemberId.equals(followingMemberId)) {
            throw new LibriException(ErrorCode.SELF_FOLLOW_NOT_ALLOWED);
        }
    }

    private void createNewFollow(Member follower, Member following) {
        Follow follow = Follow.create(follower, following);
        followRepository.save(follow);
    }

    private void reactivateOrThrow(Follow relation) {
        if(relation.getFollowStatus() == FollowStatus.FOLLOW) {
            throw new LibriException(ErrorCode.EXIST_FOLLOW_RELATION);
        }
        relation.follow();
    }

    private Member getMemberOrThrow(Long memberId) {
        return memberService.getMemberById(memberId)
                .orElseThrow(() -> new LibriException(ErrorCode.MEMBER_NOT_FOUND));
    }
    // ************* 내부 메서드 *************

    @Override
    @Transactional
    public void follow(Long followerMemberId, Long followingMemberId) {
        validateNotSelf(followerMemberId, followingMemberId);
        if (blockService.hasBlockRelation(followerMemberId, followingMemberId)) {
            throw new LibriException(ErrorCode.ACCESS_DENIED);
        }

        Member follower = getMemberOrThrow(followerMemberId);
        Member following = getMemberOrThrow(followingMemberId);

        followRepository.findByFollowerAndFollowing(follower, following)
                .ifPresentOrElse(
                        this::reactivateOrThrow,
                        () -> createNewFollow(follower, following)
                );

        log.info("follow.created followerId={} followingId={}", followerMemberId, followingMemberId);
    }

    @Override
    @Transactional
    public void unfollow(Long followerMemberId, Long followingMemberId) {
        validateNotSelf(followerMemberId, followingMemberId);

        Member follower = getMemberOrThrow(followerMemberId);
        Member following = getMemberOrThrow(followingMemberId);

        Follow relation = followRepository.findByFollowerAndFollowing(follower, following)
                .orElseThrow(() -> new LibriException(ErrorCode.FOLLOW_NOT_FOUND));

        if(relation.getFollowStatus() == FollowStatus.UNFOLLOW) {
            throw new LibriException(ErrorCode.ALREADY_UNFOLLOWED);
        }

        relation.unfollow();

        log.info("follow.deleted followerId={} followingId={}", followerMemberId, followingMemberId);
    }

    @Override
    public Slice<MemberSummaryDto> findFollowers(Long memberId, Pageable pageable) {
        Member member = getMemberOrThrow(memberId);
        return followRepository.findFollowers(member, pageable);
    }

    @Override
    public Slice<MemberSummaryDto> findFollowings(Long memberId, Pageable pageable) {
        Member member = getMemberOrThrow(memberId);
        return followRepository.findFollowings(member, pageable);
    }
}
