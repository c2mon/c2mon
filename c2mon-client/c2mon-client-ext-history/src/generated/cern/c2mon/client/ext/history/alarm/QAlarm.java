package cern.c2mon.client.ext.history.alarm;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QAlarm is a Querydsl query type for Alarm
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QAlarm extends EntityPathBase<Alarm> {

    private static final long serialVersionUID = 1157347051L;

    public static final QAlarm alarm = new QAlarm("alarm");

    public final BooleanPath active = createBoolean("active");

    public final NumberPath<Integer> faultCode = createNumber("faultCode", Integer.class);

    public final StringPath faultFamily = createString("faultFamily");

    public final StringPath faultMember = createString("faultMember");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath info = createString("info");

    public final NumberPath<Long> tagId = createNumber("tagId", Long.class);

    public final DateTimePath<java.sql.Timestamp> timestamp = createDateTime("timestamp", java.sql.Timestamp.class);

    public QAlarm(String variable) {
        super(Alarm.class, forVariable(variable));
    }

    public QAlarm(Path<? extends Alarm> path) {
        super(path.getType(), path.getMetadata());
    }

    public QAlarm(PathMetadata<?> metadata) {
        super(Alarm.class, metadata);
    }

}

