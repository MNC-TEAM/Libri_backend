package monochrome.libri.global.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QAuditableEntity is a Querydsl query type for AuditableEntity
 */
@Generated("com.querydsl.codegen.DefaultSupertypeSerializer")
public class QAuditableEntity extends EntityPathBase<AuditableEntity> {

    private static final long serialVersionUID = 1686644758L;

    public static final QAuditableEntity auditableEntity = new QAuditableEntity("auditableEntity");

    public final NumberPath<Long> createdBy = createNumber("createdBy", Long.class);

    public final DateTimePath<java.time.LocalDateTime> createdDate = createDateTime("createdDate", java.time.LocalDateTime.class);

    public final NumberPath<Long> lastModifiedBy = createNumber("lastModifiedBy", Long.class);

    public final DateTimePath<java.time.LocalDateTime> lastModifiedDate = createDateTime("lastModifiedDate", java.time.LocalDateTime.class);

    public QAuditableEntity(String variable) {
        super(AuditableEntity.class, forVariable(variable));
    }

    public QAuditableEntity(Path<? extends AuditableEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QAuditableEntity(PathMetadata metadata) {
        super(AuditableEntity.class, metadata);
    }

}

