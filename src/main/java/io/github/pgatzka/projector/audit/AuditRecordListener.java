package io.github.pgatzka.projector.audit;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.RecordContext;
import org.jooq.RecordListener;
import org.springframework.stereotype.Component;

/**
 * Sets created_by on insert and updated_by on update from the current
 * ActorContext. Audit columns the table doesn't have are silently skipped.
 */
@Component
public class AuditRecordListener implements RecordListener {

    private final ActorContext actorContext;

    public AuditRecordListener(ActorContext actorContext) {
        this.actorContext = actorContext;
    }

    @Override
    public void insertStart(RecordContext ctx) {
        String actor = actorContext.currentActor();
        setIfPresent(ctx.record(), "created_by", actor);
        setIfPresent(ctx.record(), "updated_by", actor);
    }

    @Override
    public void updateStart(RecordContext ctx) {
        setIfPresent(ctx.record(), "updated_by", actorContext.currentActor());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void setIfPresent(Record record, String name, String value) {
        Field<?> field = record.field(name);
        if (field != null) {
            record.set((Field) field, value);
        }
    }
}
