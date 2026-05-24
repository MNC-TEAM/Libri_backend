package monochrome.libri.fcm.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QFcmNotificationToken is a Querydsl query type for FcmNotificationToken
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFcmNotificationToken extends EntityPathBase<FcmNotificationToken> {

    private static final long serialVersionUID = -1610994873L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QFcmNotificationToken fcmNotificationToken = new QFcmNotificationToken("fcmNotificationToken");

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

    public final DateTimePath<java.time.LocalDateTime> lastUsedDate = createDateTime("lastUsedDate", java.time.LocalDateTime.class);

    public final monochrome.libri.member.domain.QMember member;

    public final StringPath token = createString("token");

    public QFcmNotificationToken(String variable) {
        this(FcmNotificationToken.class, forVariable(variable), INITS);
    }

    public QFcmNotificationToken(Path<? extends FcmNotificationToken> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QFcmNotificationToken(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QFcmNotificationToken(PathMetadata metadata, PathInits inits) {
        this(FcmNotificationToken.class, metadata, inits);
    }

    public QFcmNotificationToken(Class<? extends FcmNotificationToken> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new monochrome.libri.member.domain.QMember(forProperty("member")) : null;
    }

}

