package monochrome.libri.block.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMemberBlock is a Querydsl query type for MemberBlock
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMemberBlock extends EntityPathBase<MemberBlock> {

    private static final long serialVersionUID = -1411842579L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMemberBlock memberBlock = new QMemberBlock("memberBlock");

    public final monochrome.libri.global.domain.QAuditableEntity _super = new monochrome.libri.global.domain.QAuditableEntity(this);

    public final monochrome.libri.member.domain.QMember blocked;

    public final monochrome.libri.member.domain.QMember blocker;

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdDate = _super.createdDate;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final NumberPath<Long> lastModifiedBy = _super.lastModifiedBy;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> lastModifiedDate = _super.lastModifiedDate;

    public QMemberBlock(String variable) {
        this(MemberBlock.class, forVariable(variable), INITS);
    }

    public QMemberBlock(Path<? extends MemberBlock> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMemberBlock(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMemberBlock(PathMetadata metadata, PathInits inits) {
        this(MemberBlock.class, metadata, inits);
    }

    public QMemberBlock(Class<? extends MemberBlock> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.blocked = inits.isInitialized("blocked") ? new monochrome.libri.member.domain.QMember(forProperty("blocked")) : null;
        this.blocker = inits.isInitialized("blocker") ? new monochrome.libri.member.domain.QMember(forProperty("blocker")) : null;
    }

}

