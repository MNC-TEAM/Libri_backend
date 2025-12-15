package monochrome.libri.follow.domain;

import jakarta.persistence.*;
import lombok.*;
import monochrome.libri.member.domain.Member;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Follow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_follow")
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follow_id")
    private Member follow;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_id")
    private Member following;

    @Enumerated(EnumType.STRING)
    private Status status;
}
