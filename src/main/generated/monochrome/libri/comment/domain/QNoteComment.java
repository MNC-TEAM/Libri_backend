package monochrome.libri.comment.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QNoteComment is a Querydsl query type for NoteComment
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QNoteComment extends EntityPathBase<NoteComment> {

    private static final long serialVersionUID = 1988778517L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QNoteComment noteComment = new QNoteComment("noteComment");

    public final monochrome.libri.global.domain.QAuditableEntity _super = new monochrome.libri.global.domain.QAuditableEntity(this);

    public final StringPath content = createString("content");

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

    public final monochrome.libri.note.domain.QNote note;

    public QNoteComment(String variable) {
        this(NoteComment.class, forVariable(variable), INITS);
    }

    public QNoteComment(Path<? extends NoteComment> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QNoteComment(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QNoteComment(PathMetadata metadata, PathInits inits) {
        this(NoteComment.class, metadata, inits);
    }

    public QNoteComment(Class<? extends NoteComment> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new monochrome.libri.member.domain.QMember(forProperty("member")) : null;
        this.note = inits.isInitialized("note") ? new monochrome.libri.note.domain.QNote(forProperty("note"), inits.get("note")) : null;
    }

}

