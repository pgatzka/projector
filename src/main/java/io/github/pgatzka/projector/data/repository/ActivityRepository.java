package io.github.pgatzka.projector.data.repository;

import io.github.pgatzka.projector.jooq.tables.pojos.Activity;
import io.github.pgatzka.projector.jooq.tables.records.ActivityRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

import static io.github.pgatzka.projector.jooq.tables.Activity.ACTIVITY;

@Repository
public class ActivityRepository {

    private final DSLContext dsl;

    public ActivityRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public Activity insert(Activity activity) {
        ActivityRecord record = dsl.newRecord(ACTIVITY, activity);
        record.store();
        return record.into(Activity.class);
    }

    public List<Activity> findByIssueIdOrderByCreatedAtAsc(UUID issueId) {
        return dsl.selectFrom(ACTIVITY)
            .where(ACTIVITY.ISSUE_ID.eq(issueId))
            .orderBy(ACTIVITY.CREATED_AT.asc())
            .fetchInto(Activity.class);
    }
}
