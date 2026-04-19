package monochrome.libri.note.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QNoteLike is a Querydsl query type for NoteLike
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QNoteLike extends EntityPathBase<NoteLike> {

    private static final long serialVersionUID = -1688456358L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QNoteLike noteLike = new QNoteLike("noteLike");

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

    public final QNote note;

    public QNoteLike(String variable) {
        this(NoteLike.class, forVariable(variable), INITS);
    }

    public QNoteLike(Path<? extends NoteLike> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QNoteLike(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QNoteLike(PathMetadata metadata, PathInits inits) {
        this(NoteLike.class, metadata, inits);
    }

    public QNoteLike(Class<? extends NoteLike> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new monochrome.libri.member.domain.QMember(forProperty("member")) : null;
        this.note = inits.isInitialized("note") ? new QNote(forProperty("note"), inits.get("note")) : null;
    }

}

