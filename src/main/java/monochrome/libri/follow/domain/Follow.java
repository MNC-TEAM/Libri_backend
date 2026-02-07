package monochrome.libri.follow.domain;

import jakarta.persistence.*;
import lombok.*;
import monochrome.libri.global.domain.AuditableEntity;
import monochrome.libri.member.domain.Member;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Follow extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_follow")
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follow_id")
    private Member follower;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_id")
    private Member following;

    @Enumerated(EnumType.STRING)
    private FollowStatus followStatus;

    public static Follow create(Member follower, Member following) {
        return Follow.builder()
                .follower(follower)
                .following(following)
                .followStatus(FollowStatus.FOLLOW)
                .build();
    }

    public void unfollow() {
        this.followStatus = FollowStatus.UNFOLLOW;
    }
    public void follow() {
        this.followStatus = FollowStatus.FOLLOW;
    }
}
