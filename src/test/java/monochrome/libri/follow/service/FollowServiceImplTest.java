package monochrome.libri.follow.service;

import monochrome.libri.TestFixtures;
import monochrome.libri.block.service.BlockService;
import monochrome.libri.follow.domain.Follow;
import monochrome.libri.follow.domain.FollowStatus;
import monochrome.libri.follow.dto.MemberSummaryDto;
import monochrome.libri.follow.repository.FollowRepository;
import monochrome.libri.global.exception.LibriException;
import monochrome.libri.member.domain.Member;
import monochrome.libri.member.service.MemberService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.SliceImpl;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FollowServiceImplTest {

    @Mock
    private FollowRepository followRepository;

    @Mock
    private MemberService memberService;

    @Mock
    private BlockService blockService;

    @InjectMocks
    private FollowServiceImpl service;

    @Test
    void follow_rejectsSelf() {
        assertThatThrownBy(() -> service.follow(1L, 1L))
                .isInstanceOf(LibriException.class);
    }

    @Test
    void follow_reactivatesWhenUnfollowed() {
        Member follower = TestFixtures.member(1L);
        Member following = TestFixtures.member(2L);
        Follow relation = TestFixtures.follow(follower, following, FollowStatus.UNFOLLOW);

        when(memberService.getMemberById(1L)).thenReturn(Optional.of(follower));
        when(memberService.getMemberById(2L)).thenReturn(Optional.of(following));
        when(blockService.hasBlockRelation(1L, 2L)).thenReturn(false);
        when(followRepository.findByFollowerAndFollowing(follower, following)).thenReturn(Optional.of(relation));

        service.follow(1L, 2L);

        assertThat(relation.getFollowStatus()).isEqualTo(FollowStatus.FOLLOW);
        verify(followRepository, never()).save(any());
    }

    @Test
    void follow_throwsWhenAlreadyFollow() {
        Member follower = TestFixtures.member(1L);
        Member following = TestFixtures.member(2L);
        Follow relation = TestFixtures.follow(follower, following, FollowStatus.FOLLOW);

        when(memberService.getMemberById(1L)).thenReturn(Optional.of(follower));
        when(memberService.getMemberById(2L)).thenReturn(Optional.of(following));
        when(blockService.hasBlockRelation(1L, 2L)).thenReturn(false);
        when(followRepository.findByFollowerAndFollowing(follower, following)).thenReturn(Optional.of(relation));

        assertThatThrownBy(() -> service.follow(1L, 2L))
                .isInstanceOf(LibriException.class);
    }

    @Test
    void follow_throwsWhenBlocked() {
        when(blockService.hasBlockRelation(1L, 2L)).thenReturn(true);

        assertThatThrownBy(() -> service.follow(1L, 2L))
                .isInstanceOf(LibriException.class);
        verifyNoInteractions(memberService, followRepository);
    }

    @Test
    void unfollow_changesStatus() {
        Member follower = TestFixtures.member(1L);
        Member following = TestFixtures.member(2L);
        Follow relation = TestFixtures.follow(follower, following, FollowStatus.FOLLOW);

        when(memberService.getMemberById(1L)).thenReturn(Optional.of(follower));
        when(memberService.getMemberById(2L)).thenReturn(Optional.of(following));
        when(followRepository.findByFollowerAndFollowing(follower, following)).thenReturn(Optional.of(relation));

        service.unfollow(1L, 2L);

        assertThat(relation.getFollowStatus()).isEqualTo(FollowStatus.UNFOLLOW);
    }

    @Test
    void unfollow_throwsWhenAlreadyUnfollowed() {
        Member follower = TestFixtures.member(1L);
        Member following = TestFixtures.member(2L);
        Follow relation = TestFixtures.follow(follower, following, FollowStatus.UNFOLLOW);

        when(memberService.getMemberById(1L)).thenReturn(Optional.of(follower));
        when(memberService.getMemberById(2L)).thenReturn(Optional.of(following));
        when(followRepository.findByFollowerAndFollowing(follower, following)).thenReturn(Optional.of(relation));

        assertThatThrownBy(() -> service.unfollow(1L, 2L))
                .isInstanceOf(LibriException.class);
    }

    @Test
    void findFollowers_returnsRepositorySlice() {
        Member member = TestFixtures.member(1L);
        var pageable = PageRequest.of(0, 20);
        var expected = new SliceImpl<>(List.of(
                new MemberSummaryDto(2L, "user2", "nick2", "/profile/2")
        ), pageable, false);

        when(memberService.getMemberById(1L)).thenReturn(Optional.of(member));
        when(followRepository.findFollowers(member, pageable)).thenReturn(expected);

        var result = service.findFollowers(1L, pageable);

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).memberId()).isEqualTo(2L);
        assertThat(result.hasNext()).isFalse();
        verify(followRepository).findFollowers(member, pageable);
    }

    @Test
    void findFollowings_returnsRepositorySlice() {
        Member member = TestFixtures.member(1L);
        var pageable = PageRequest.of(0, 20);
        var expected = new SliceImpl<>(List.of(
                new MemberSummaryDto(3L, "user3", "nick3", "/profile/3")
        ), pageable, false);

        when(memberService.getMemberById(1L)).thenReturn(Optional.of(member));
        when(followRepository.findFollowings(member, pageable)).thenReturn(expected);

        var result = service.findFollowings(1L, pageable);

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).memberId()).isEqualTo(3L);
        assertThat(result.page()).isEqualTo(0);
        verify(followRepository).findFollowings(member, pageable);
    }
}
