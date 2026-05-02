package monochrome.libri.member.service.impl;

import monochrome.libri.block.service.BlockService;
import monochrome.libri.follow.domain.FollowStatus;
import monochrome.libri.follow.repository.FollowRepository;
import monochrome.libri.global.exception.ErrorCode;
import monochrome.libri.global.exception.LibriException;
import monochrome.libri.global.security.PasswordHashService;
import monochrome.libri.fcm.domain.FcmNotificationToken;
import monochrome.libri.fcm.repository.FcmNotificationTokenRepository;
import monochrome.libri.member.domain.Member;
import monochrome.libri.member.dto.request.MemberUpdateRequestDto;
import monochrome.libri.member.dto.response.MemberPrivacyResponseDto;
import monochrome.libri.member.dto.response.MemberProfileResponseDto;
import monochrome.libri.member.dto.response.MemberResponseDto;
import monochrome.libri.member.repository.MemberRepository;
import monochrome.libri.member.service.MemberService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
    private final FollowRepository followRepository;
    private final BlockService blockService;
    private final PasswordHashService passwordHashService;
    private final FcmNotificationTokenRepository fcmNotificationTokenRepository;

    public MemberServiceImpl(
            MemberRepository memberRepository,
            FollowRepository followRepository,
            BlockService blockService,
            PasswordHashService passwordHashService,
            FcmNotificationTokenRepository fcmNotificationTokenRepository
    )  {
        this.memberRepository = memberRepository;
        this.followRepository = followRepository;
        this.blockService = blockService;
        this.passwordHashService = passwordHashService;
        this.fcmNotificationTokenRepository = fcmNotificationTokenRepository;
    }

    @Override
    @Transactional
    public Member updateMember(long memberId, MemberUpdateRequestDto memberUpdateRequestDto) {

        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new LibriException(ErrorCode.MEMBER_NOT_FOUND)
        );

        String passwordHash = member.getPasswordHash();

        // dto에 비밀번호가 들어온 경우 업데이트
        if(memberUpdateRequestDto.rawPassword() != null && !memberUpdateRequestDto.rawPassword().isBlank()) {
            passwordHash = passwordHashService.hashPassword(memberUpdateRequestDto.rawPassword());
        }

        return member.updateMember(
                memberUpdateRequestDto.username(),
                memberUpdateRequestDto.nickname(),
                passwordHash,
                memberUpdateRequestDto.profilePath(),
                memberUpdateRequestDto.privateAccount()
        );
    }

    @Override
    public Optional<Member> getMemberById(long memberId) {
        return memberRepository.findById(memberId);
    }

    @Override
    public Optional<Member> getMemberByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    @Override
    public MemberResponseDto getMyProfile(long memberId) {
        Member member = getRequiredMember(memberId);
        return MemberResponseDto.from(
                member,
                followRepository.countByFollowingAndFollowStatus(member, FollowStatus.FOLLOW),
                followRepository.countByFollowerAndFollowStatus(member, FollowStatus.FOLLOW)
        );
    }

    @Override
    public MemberPrivacyResponseDto getMyPrivacy(long memberId) {
        Member member = getRequiredMember(memberId);
        return new MemberPrivacyResponseDto(member.getId(), member.isPrivateAccount());
    }

    @Override
    public MemberProfileResponseDto getMemberProfile(Long actorMemberId, long targetMemberId) {
        Member target = getRequiredMember(targetMemberId);
        if (actorMemberId != null && actorMemberId > 0 && blockService.hasBlockRelation(actorMemberId, targetMemberId)) {
            throw new LibriException(ErrorCode.ACCESS_DENIED);
        }
        long followerCount = followRepository.countByFollowingAndFollowStatus(target, FollowStatus.FOLLOW);
        long followingCount = followRepository.countByFollowerAndFollowStatus(target, FollowStatus.FOLLOW);
        boolean mine = actorMemberId != null && actorMemberId == targetMemberId;
        boolean following = false;

        if (!mine && actorMemberId != null && actorMemberId > 0) {
            Member actor = getRequiredMember(actorMemberId);
            following = followRepository.findByFollowerAndFollowing(actor, target)
                    .map(relation -> relation.getFollowStatus() == FollowStatus.FOLLOW)
                    .orElse(false);
        }

        return MemberProfileResponseDto.from(target, followerCount, followingCount, mine, following);
    }

    @Override
    @Transactional
    public void withdraw(Long memberId) {
        Member member = getRequiredMember(memberId);

        fcmNotificationTokenRepository.deleteByMember_Id(memberId);
        member.withdraw();
    }

    private Member getRequiredMember(long memberId) {
        return getMemberById(memberId)
                .orElseThrow(() -> new LibriException(ErrorCode.MEMBER_NOT_FOUND));
    }

    @Override
    @Transactional
    public void updateFcmRegistrationToken(long memberId, String token) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new LibriException(ErrorCode.MEMBER_NOT_FOUND));
        if (token == null || token.isBlank()) {
            fcmNotificationTokenRepository.deleteByMember_Id(memberId);
            return;
        }
        String trimmed = token.trim();
        fcmNotificationTokenRepository.findByMember_IdAndToken(memberId, trimmed)
                .ifPresentOrElse(
                        FcmNotificationToken::touchLastUsed,
                        () -> fcmNotificationTokenRepository.save(FcmNotificationToken.create(member, trimmed))
                );
    }
}
