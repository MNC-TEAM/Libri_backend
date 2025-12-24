package monochrome.libri.follow.service;

import monochrome.libri.config.QuerydslConfig;
import monochrome.libri.follow.domain.Follow;
import monochrome.libri.follow.dto.MemberSummaryDto;
import monochrome.libri.follow.repository.FollowRepository;
import monochrome.libri.member.domain.Member;
import monochrome.libri.member.domain.MemberStatus;
import monochrome.libri.member.domain.Role;
import monochrome.libri.member.domain.SignType;
import monochrome.libri.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;


@DataJpaTest
@Import(QuerydslConfig.class)
public class FollowRepositoryDataJpaTest {

    @Autowired FollowRepository followRepository;
    @Autowired MemberRepository memberRepository;

    private Member member(Long id) {
        return Member.builder()
                .provider(SignType.EMAIL)
                .providerUserId("providerUserId" + id)
                .primaryEmail("email" + id + "@example.com")
                .emailVerified(false)
                .emailFromProvider(null)
                .emailVerifiedFromProvider(null)
                .username("username" + id)
                .nickname("nickname" + id)
                .passwordHash("hashedPassword" + id)
                .profilePath(null)
                .memberStatus(MemberStatus.ACTIVE)
                .role(Role.USER)
                .build();
    }

    @Test
    @DisplayName("팔로워 목록 조회 시 FOLLOW 상태의 팔로워만 반환한다(UNFOLLOW 제외).")
    void getFollowers_success_returnsOnlyFOLLOW() {
        // given
        Member target = memberRepository.save(member(1L));     // 네 builder 방식에 맞춰 생성
        Member follower1 = memberRepository.save(member(2L));
        Member follower2 = memberRepository.save(member(3L));

        Follow f1 = Follow.create(follower1, target); // FOLLOW
        followRepository.save(f1);

        Follow f2 = Follow.create(follower2, target);
        f2.unfollow();
        followRepository.save(f2);

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Slice<MemberSummaryDto> result = followRepository.findFollowers(target, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).memberId()).isEqualTo(follower1.getId());
    }

    @Test
    @DisplayName("팔로잉 목록 조회 시 FOLLOW 상태의 팔로워만 반환한다(UNFOLLOW 제외).")
    void getFollowings_success_returnsOnlyFOLLOW() {
        // given
        Member target = memberRepository.save(member(1L));
        Member following1 = memberRepository.save(member(2L));
        Member following2 = memberRepository.save(member(3L));

        Follow f1 = Follow.create(target, following1); // FOLLOW
        followRepository.save(f1);

        Follow f2 = Follow.create(target, following2);
        f2.unfollow(); // UNFOLLOW
        followRepository.save(f2);

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Slice<MemberSummaryDto> result = followRepository.findFollowings(target, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).memberId()).isEqualTo(following1.getId());
    }
}
