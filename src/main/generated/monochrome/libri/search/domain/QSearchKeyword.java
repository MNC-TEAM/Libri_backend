package monochrome.libri.search.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSearchKeyword is a Querydsl query type for SearchKeyword
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSearchKeyword extends EntityPathBase<SearchKeyword> {

    private static final long serialVersionUID = -1677787686L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSearchKeyword searchKeyword = new QSearchKeyword("searchKeyword");

    public final monochrome.libri.global.domain.QAuditableEntity _super = new monochrome.libri.global.domain.QAuditableEntity(this);

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdDate = _super.createdDate;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath keyword = createString("keyword");

    //inherited
    public final NumberPath<Long> lastModifiedBy = _super.lastModifiedBy;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> lastModifiedDate = _super.lastModifiedDate;

    public final monochrome.libri.member.domain.QMember member;

    public QSearchKeyword(String variable) {
        this(SearchKeyword.class, forVariable(variable), INITS);
    }

    public QSearchKeyword(Path<? extends SearchKeyword> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSearchKeyword(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSearchKeyword(PathMetadata metadata, PathInits inits) {
        this(SearchKeyword.class, metadata, inits);
    }

    public QSearchKeyword(Class<? extends SearchKeyword> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new monochrome.libri.member.domain.QMember(forProperty("member")) : null;
    }

}

