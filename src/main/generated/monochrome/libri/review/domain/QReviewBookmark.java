package monochrome.libri.review.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QReviewBookmark is a Querydsl query type for ReviewBookmark
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QReviewBookmark extends EntityPathBase<ReviewBookmark> {

    private static final long serialVersionUID = 968151749L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QReviewBookmark reviewBookmark = new QReviewBookmark("reviewBookmark");

    public final monochrome.libri.global.domain.QAuditableEntity _super = new monochrome.libri.global.domain.QAuditableEntity(this);

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdDate = _super.createdDate;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final NumberPath<Long> lastModifiedBy = _super.lastModifiedBy;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> lastModifiedDate = _super.lastModifiedDate;

    public final monochrome.libri.member.domain.QMember member;

    public final QReview review;

    public QReviewBookmark(String variable) {
        this(ReviewBookmark.class, forVariable(variable), INITS);
    }

    public QReviewBookmark(Path<? extends ReviewBookmark> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QReviewBookmark(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QReviewBookmark(PathMetadata metadata, PathInits inits) {
        this(ReviewBookmark.class, metadata, inits);
    }

    public QReviewBookmark(Class<? extends ReviewBookmark> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new monochrome.libri.member.domain.QMember(forProperty("member")) : null;
        this.review = inits.isInitialized("review") ? new QReview(forProperty("review"), inits.get("review")) : null;
    }

}

