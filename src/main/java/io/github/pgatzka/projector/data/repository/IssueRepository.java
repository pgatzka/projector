package io.github.pgatzka.projector.data.repository;

import io.github.pgatzka.projector.jooq.tables.pojos.Issue;
import io.github.pgatzka.projector.jooq.tables.records.IssueRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static io.github.pgatzka.projector.jooq.tables.Issue.ISSUE;

@Repository
public class IssueRepository {

    private final DSLContext dsl;

    public IssueRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public List<Issue> findByProjectId(UUID projectId) {
        return dsl.selectFrom(ISSUE)
            .where(ISSUE.PROJECT_ID.eq(projectId))
            .orderBy(ISSUE.NUMBER.desc())
            .fetchInto(Issue.class);
    }

    public Optional<Issue> findById(UUID id) {
        return dsl.selectFrom(ISSUE).where(ISSUE.ID.eq(id)).fetchOptionalInto(Issue.class);
    }

    public Optional<Issue> findByProjectIdAndNumber(UUID projectId, int number) {
        return dsl.selectFrom(ISSUE)
            .where(ISSUE.PROJECT_ID.eq(projectId).and(ISSUE.NUMBER.eq(number)))
            .fetchOptionalInto(Issue.class);
    }

    public Issue insert(Issue issue) {
        IssueRecord record = dsl.newRecord(ISSUE, issue);
        record.store();
        return record.into(Issue.class);
    }

    public Issue update(Issue issue) {
        IssueRecord record = dsl.newRecord(ISSUE, issue);
        record.update();
        return record.into(Issue.class);
    }

    public int deleteById(UUID id) {
        return dsl.deleteFrom(ISSUE).where(ISSUE.ID.eq(id)).execute();
    }
}
