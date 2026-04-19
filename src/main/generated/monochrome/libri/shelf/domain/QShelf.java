package monochrome.libri.shelf.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QShelf is a Querydsl query type for Shelf
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QShelf extends EntityPathBase<Shelf> {

    private static final long serialVersionUID = -1993762041L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QShelf shelf = new QShelf("shelf");

    public final monochrome.libri.global.domain.QAuditableEntity _super = new monochrome.libri.global.domain.QAuditableEntity(this);

    public final monochrome.libri.book.domain.QBook book;

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdDate = _super.createdDate;

    public final NumberPath<Integer> currentPage = createNumber("currentPage", Integer.class);

    public final DatePath<java.time.LocalDate> endDate = createDate("endDate", java.time.LocalDate.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final NumberPath<Long> lastModifiedBy = _super.lastModifiedBy;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> lastModifiedDate = _super.lastModifiedDate;

    public final monochrome.libri.member.domain.QMember member;

    public final EnumPath<ShelfProgressType> progressType = createEnum("progressType", ShelfProgressType.class);

    public final NumberPath<Integer> progressValue = createNumber("progressValue", Integer.class);

    public final DatePath<java.time.LocalDate> startDate = createDate("startDate", java.time.LocalDate.class);

    public final EnumPath<ShelfStatus> status = createEnum("status", ShelfStatus.class);

    public QShelf(String variable) {
        this(Shelf.class, forVariable(variable), INITS);
    }

    public QShelf(Path<? extends Shelf> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QShelf(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QShelf(PathMetadata metadata, PathInits inits) {
        this(Shelf.class, metadata, inits);
    }

    public QShelf(Class<? extends Shelf> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.book = inits.isInitialized("book") ? new monochrome.libri.book.domain.QBook(forProperty("book"), inits.get("book")) : null;
        this.member = inits.isInitialized("member") ? new monochrome.libri.member.domain.QMember(forProperty("member")) : null;
    }

}

