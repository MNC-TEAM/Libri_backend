package monochrome.libri.follow.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import monochrome.libri.follow.domain.FollowStatus;
import monochrome.libri.follow.domain.QFollow;
import monochrome.libri.follow.dto.MemberSummaryDto;
import monochrome.libri.member.domain.Member;
import monochrome.libri.member.domain.QMember;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

public class FollowRepositoryImpl implements FollowRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public FollowRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public Slice<MemberSummaryDto> findFollowers(Member following, Pageable pageable) {
        QFollow qf = QFollow.follow;
        QMember follower = new QMember("follower");

        int pageSize = pageable.getPageSize();

        List<MemberSummaryDto> content = queryFactory
                .select(Projections.constructor(
                    MemberSummaryDto.class,
                    follower.id,
                    follower.username,
                    follower.nickname,
                    follower.profilePath
                ))
                .from(qf)
                .join(qf.follower, follower)
                .where(
                        qf.following.eq(following),
                        qf.followStatus.eq(FollowStatus.FOLLOW)
                )
                .offset(pageable.getOffset())
                .limit(pageSize + 1)
                .fetch();

        boolean hasNext = content.size() > pageSize;
        if(hasNext) {
            content.remove(pageSize);
        }

        return new SliceImpl<>(content, pageable, hasNext);
    }

    @Override
    public Slice<MemberSummaryDto> findFollowings(Member follower, Pageable pageable) {
        QFollow qf = QFollow.follow;
        QMember following = new QMember("following");

        int pageSize = pageable.getPageSize();

        List<MemberSummaryDto> content = queryFactory
                .select(Projections.constructor(
                        MemberSummaryDto.class,
                        following.id,
                        following.username,
                        following.nickname,
                        following.profilePath
                ))
                .from(qf)
                .join(qf.following, following)
                .where(
                        qf.follower.eq(follower),
                        qf.followStatus.eq(FollowStatus.FOLLOW)
                )
                .offset(pageable.getOffset())
                .limit(pageSize + 1)
                .fetch();

        boolean hasNext = content.size() > pageSize;
        if(hasNext) {
            content.remove(pageSize);
        }

        return new SliceImpl<>(content, pageable, hasNext);
    }
}
