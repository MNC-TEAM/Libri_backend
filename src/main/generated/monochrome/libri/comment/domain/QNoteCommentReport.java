package monochrome.libri.comment.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QNoteCommentReport is a Querydsl query type for NoteCommentReport
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QNoteCommentReport extends EntityPathBase<NoteCommentReport> {

    private static final long serialVersionUID = -1066207319L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QNoteCommentReport noteCommentReport = new QNoteCommentReport("noteCommentReport");

    public final monochrome.libri.global.domain.QAuditableEntity _super = new monochrome.libri.global.domain.QAuditableEntity(this);

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdDate = _super.createdDate;

    public final StringPath detail = createString("detail");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final NumberPath<Long> lastModifiedBy = _super.lastModifiedBy;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> lastModifiedDate = _super.lastModifiedDate;

    public final QNoteComment noteComment;

    public final EnumPath<CommentReportReason> reason = createEnum("reason", CommentReportReason.class);

    public final monochrome.libri.member.domain.QMember reporter;

    public QNoteCommentReport(String variable) {
        this(NoteCommentReport.class, forVariable(variable), INITS);
    }

    public QNoteCommentReport(Path<? extends NoteCommentReport> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QNoteCommentReport(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QNoteCommentReport(PathMetadata metadata, PathInits inits) {
        this(NoteCommentReport.class, metadata, inits);
    }

    public QNoteCommentReport(Class<? extends NoteCommentReport> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.noteComment = inits.isInitialized("noteComment") ? new QNoteComment(forProperty("noteComment"), inits.get("noteComment")) : null;
        this.reporter = inits.isInitialized("reporter") ? new monochrome.libri.member.domain.QMember(forProperty("reporter")) : null;
    }

}

