package cern.c2mon.client.ext.history.lifecycle;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QServerLifecycleEvent is a Querydsl query type for ServerLifecycleEvent
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QServerLifecycleEvent extends EntityPathBase<ServerLifecycleEvent> {

    private static final long serialVersionUID = 354055072L;

    public static final QServerLifecycleEvent serverLifecycleEvent = new QServerLifecycleEvent("serverLifecycleEvent");

    public final DateTimePath<java.util.Date> eventTime = createDateTime("eventTime", java.util.Date.class);

    public final StringPath eventType = createString("eventType");

    public final StringPath serverName = createString("serverName");

    public QServerLifecycleEvent(String variable) {
        super(ServerLifecycleEvent.class, forVariable(variable));
    }

    public QServerLifecycleEvent(Path<? extends ServerLifecycleEvent> path) {
        super(path.getType(), path.getMetadata());
    }

    public QServerLifecycleEvent(PathMetadata<?> metadata) {
        super(ServerLifecycleEvent.class, metadata);
    }

}

