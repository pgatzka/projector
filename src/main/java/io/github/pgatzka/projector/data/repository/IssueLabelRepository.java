package io.github.pgatzka.projector.data.repository;

import io.github.pgatzka.projector.jooq.tables.pojos.IssueLabel;
import io.github.pgatzka.projector.jooq.tables.records.IssueLabelRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static io.github.pgatzka.projector.jooq.tables.IssueLabel.ISSUE_LABEL;

@Repository
public class IssueLabelRepository {

    private final DSLContext dsl;

    public IssueLabelRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public List<IssueLabel> findByIssueId(UUID issueId) {
        return dsl.selectFrom(ISSUE_LABEL)
            .where(ISSUE_LABEL.ISSUE_ID.eq(issueId))
            .fetchInto(IssueLabel.class);
    }

    public Optional<IssueLabel> findByIssueIdAndLabelId(UUID issueId, UUID labelId) {
        return dsl.selectFrom(ISSUE_LABEL)
            .where(ISSUE_LABEL.ISSUE_ID.eq(issueId).and(ISSUE_LABEL.LABEL_ID.eq(labelId)))
            .fetchOptionalInto(IssueLabel.class);
    }

    public IssueLabel insert(IssueLabel issueLabel) {
        IssueLabelRecord record = dsl.newRecord(ISSUE_LABEL, issueLabel);
        record.store();
        return record.into(IssueLabel.class);
    }

    public int deleteByIssueIdAndLabelId(UUID issueId, UUID labelId) {
        return dsl.deleteFrom(ISSUE_LABEL)
            .where(ISSUE_LABEL.ISSUE_ID.eq(issueId).and(ISSUE_LABEL.LABEL_ID.eq(labelId)))
            .execute();
    }

    public int countByLabelId(UUID labelId) {
        return dsl.fetchCount(ISSUE_LABEL, ISSUE_LABEL.LABEL_ID.eq(labelId));
    }

    public List<UUID> findIssueIdsByLabelId(UUID labelId) {
        return dsl.select(ISSUE_LABEL.ISSUE_ID)
            .from(ISSUE_LABEL)
            .where(ISSUE_LABEL.LABEL_ID.eq(labelId))
            .fetchInto(UUID.class);
    }

    public int bulkInsert(UUID issueId, List<UUID> labelIds) {
        if (issueId == null || labelIds == null || labelIds.isEmpty()) {
            return 0;
        }
        int inserted = 0;
        for (UUID labelId : labelIds) {
            IssueLabel il = new IssueLabel();
            il.setIssueId(issueId);
            il.setLabelId(labelId);
            IssueLabelRecord record = dsl.newRecord(ISSUE_LABEL, il);
            record.store();
            inserted++;
        }
        return inserted;
    }
}
