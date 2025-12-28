package monochrome.libri.follow.service;

import monochrome.libri.follow.domain.Follow;
import monochrome.libri.follow.domain.FollowStatus;
import monochrome.libri.follow.repository.FollowRepository;
import monochrome.libri.global.exception.ErrorCode;
import monochrome.libri.global.exception.LibriException;
import monochrome.libri.member.domain.Member;
import monochrome.libri.member.domain.MemberStatus;
import monochrome.libri.member.domain.Role;
import monochrome.libri.member.domain.SignType;
import monochrome.libri.member.service.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FollowServiceTest {

    @Mock FollowRepository followRepository;
    @Mock MemberService memberService;
    @InjectMocks FollowServiceImpl followService;

    private Member member(Long id) {
        return Member.builder()
                .provider(SignType.EMAIL)
                .providerUserId("providerUserId" + id)
                .emailVerified(false)
                .emailVerifiedFromProvider(null)
                .username("username" + id)
                .nickname("nickname" + id)
                .passwordHash("hashedPassword" + id)
                .profilePath(null)
                .memberStatus(MemberStatus.ACTIVE)
                .role(Role.USER)
                .build();
    }

    @Nested
    @DisplayName("unfollow")
    class unfollow{

        @Test
        @DisplayName("관계가 존재하면 언팔로우 시 followStatus=UNFOLLOW로 변경한다(soft delete).")
        void unfollow_success_setsUNFOLLOW_whenRelationExists() {
            // given
            long followerId = 1L;
            long followingId = 2L;
            Member follower = member(followerId);
            Member following = member(followingId);
            Follow existing = Follow.create(follower, following);

            given(memberService.getMemberById(followerId)).willReturn(Optional.of(follower));
            given(memberService.getMemberById(followingId)).willReturn(Optional.of(following));
            given(followRepository.findByFollowerAndFollowing(follower, following))
                    .willReturn(Optional.of(existing));

            // when
            followService.unfollow(followerId, followingId);

            // then
            assertThat(existing.getFollowStatus()).isEqualTo(FollowStatus.UNFOLLOW);
        }

        @Test
        @DisplayName("관계가 없으면 FOLLOW_RELATION_NOT_FOUND 예외를 던진다.")
        void unfollow_throws_whenRelationNotFound() {
            // given
            long followerId = 1L;
            long followingId = 2L;
            Member follower = member(followerId);
            Member following = member(followingId);

            given(memberService.getMemberById(followerId)).willReturn(Optional.of(follower));
            given(memberService.getMemberById(followingId)).willReturn(Optional.of(following));
            given(followRepository.findByFollowerAndFollowing(follower, following))
                    .willReturn(Optional.empty());

            // when
            LibriException ex = assertThrows(LibriException.class, () -> {
                followService.unfollow(followerId, followingId);
            });

            // then
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.FOLLOW_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("follow")
    class follow {

        @Test
        @DisplayName("신규 관계면 팔로우 관계를 생성하고 followStatus=FOLLOW로 저장한다.")
        void follow_success_createsFollow_whenNewRelation() {
            // given
            long followerId = 1L;
            long followingId = 2L;

            Member follower = member(followerId);
            Member following = member(followingId);

            given(memberService.getMemberById(followerId)).willReturn(Optional.of(follower));
            given(memberService.getMemberById(followingId)).willReturn(Optional.of(following));

            // when
            followService.follow(followerId, followingId);

            // then

            // 실제 저장된 Follow 객체 검증
            ArgumentCaptor<Follow> captor = ArgumentCaptor.forClass(Follow.class);
            verify(followRepository).save(captor.capture());
            Follow saved = captor.getValue();
            assertThat(saved.getFollowStatus()).isEqualTo(FollowStatus.FOLLOW);
            assertThat(saved.getFollower()).isEqualTo(follower);
            assertThat(saved.getFollowing()).isEqualTo(following);
        }

        @Test
        @DisplayName("기존 관계가 UNFOLLOW면 재팔로우 시 followStatus=FOLLOW로 재활성화한다.")
        void follow_success_reactivates_whenRelationUNFOLLOW() {
            // given
            long followerId = 1L;
            long followingId = 2L;
            Member follower = member(followerId);
            Member following = member(followingId);
            Follow existing = Follow.create(follower, following);
            existing.unfollow();

            given(memberService.getMemberById(followerId)).willReturn(Optional.of(follower));
            given(memberService.getMemberById(followingId)).willReturn(Optional.of(following));
            given(followRepository.findByFollowerAndFollowing(follower, following))
                    .willReturn(Optional.of(existing));

            // when
            followService.follow(followerId, followingId);

            // then
            assertThat(existing.getFollowStatus()).isEqualTo(FollowStatus.FOLLOW);
        }

        @Test
        @DisplayName("이미 FOLLOW인 관계에 대해 팔로우 요청 시 FOLLOW_ALREADY_EXISTS 예외를 던진다.")
        void follow_throws_whenAlreadyFOLLOWRelation() {
            // given
            long followerId = 1L;
            long followingId = 2L;
            Member follower = member(followerId);
            Member following = member(followingId);
            Follow existing = Follow.create(follower, following);

            given(memberService.getMemberById(followerId)).willReturn(Optional.of(follower));
            given(memberService.getMemberById(followingId)).willReturn(Optional.of(following));
            given(followRepository.findByFollowerAndFollowing(follower, following))
                    .willReturn(Optional.of(existing));

            // when
            LibriException ex = assertThrows(LibriException.class, () -> {
                followService.follow(followerId, followingId);
            });

            // then
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.EXIST_FOLLOW_RELATION);
        }

        @Test
        @DisplayName("자기 자신을 팔로우하려 하면 SELF_FOLLOW_NOT_ALLOWED 예외를 던진다.")
        void follow_throws_whenSelfFollow() {
            // given
            long memberId = 1L;
            Member member = member(memberId);

            // when
            LibriException ex = assertThrows(LibriException.class, () -> {
                followService.follow(memberId, memberId);
            });

            // then
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.SELF_FOLLOW_NOT_ALLOWED);
        }

        @Test
        @DisplayName("following 회원이 없으면 MEMBER_NOT_FOUND 예외를 던진다.")
        void follow_throws_whenFollowingNotFound() {
            // given
            long followerId = 1L;
            long followingId = 2L;

            Member follower = member(followerId);

            given(memberService.getMemberById(followerId)).willReturn(Optional.of(follower));
            given(memberService.getMemberById(followingId)).willReturn(Optional.empty());

            // when
            LibriException ex = assertThrows(LibriException.class, () -> {
                followService.follow(followerId, followingId);
            });

            // then
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
        }

        @Test
        @DisplayName("follower 회원이 없으면 MEMBER_NOT_FOUND 예외를 던진다.")
        void follow_throws_whenFollowerNotFound() {
            // given
            long followerId = 1L;
            long followingId = 2L;

            Member following = member(followerId);

            given(memberService.getMemberById(followerId)).willReturn(Optional.empty());

            // when
            LibriException ex = assertThrows(LibriException.class, () -> {
                followService.follow(followerId, followingId);
            });

            // then
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
        }

    }
}