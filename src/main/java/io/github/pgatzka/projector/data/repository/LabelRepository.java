package io.github.pgatzka.projector.data.repository;

import io.github.pgatzka.projector.jooq.tables.pojos.Label;
import io.github.pgatzka.projector.jooq.tables.records.LabelRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static io.github.pgatzka.projector.jooq.tables.Label.LABEL;
import static org.jooq.impl.DSL.lower;

@Repository
public class LabelRepository {

    private final DSLContext dsl;

    public LabelRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public List<Label> findByProjectId(UUID projectId) {
        return dsl.selectFrom(LABEL)
            .where(LABEL.PROJECT_ID.eq(projectId))
            .orderBy(LABEL.NAME.asc())
            .fetchInto(Label.class);
    }

    public Optional<Label> findById(UUID id) {
        return dsl.selectFrom(LABEL).where(LABEL.ID.eq(id)).fetchOptionalInto(Label.class);
    }

    public Optional<Label> findByProjectIdAndNameIgnoreCase(UUID projectId, String name) {
        return dsl.selectFrom(LABEL)
            .where(LABEL.PROJECT_ID.eq(projectId).and(lower(LABEL.NAME).eq(lower(name))))
            .fetchOptionalInto(Label.class);
    }

    public Label insert(Label label) {
        LabelRecord record = dsl.newRecord(LABEL, label);
        record.store();
        record.refresh();
        return record.into(Label.class);
    }

    public Label update(Label label) {
        LabelRecord record = dsl.newRecord(LABEL, label);
        record.update();
        record.refresh();
        return record.into(Label.class);
    }

    public int deleteById(UUID id) {
        return dsl.deleteFrom(LABEL).where(LABEL.ID.eq(id)).execute();
    }
}
