package io.github.pgatzka.projector.data.repository;

import io.github.pgatzka.projector.jooq.tables.pojos.Issue;
import io.github.pgatzka.projector.jooq.tables.pojos.Label;
import io.github.pgatzka.projector.jooq.tables.records.IssueRecord;
import io.github.pgatzka.projector.rest.dto.IssueListQuery;
import io.github.pgatzka.projector.rest.dto.PageDto;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static io.github.pgatzka.projector.jooq.tables.Issue.ISSUE;
import static io.github.pgatzka.projector.jooq.tables.IssueLabel.ISSUE_LABEL;
import static io.github.pgatzka.projector.jooq.tables.Label.LABEL;

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
        record.changed(ISSUE.SEARCH_TSV, false);
        record.store();
        record.refresh();
        return record.into(Issue.class);
    }

    public Issue update(Issue issue) {
        IssueRecord record = dsl.newRecord(ISSUE, issue);
        record.changed(ISSUE.SEARCH_TSV, false);
        record.update();
        record.refresh();
        return record.into(Issue.class);
    }

    public int deleteById(UUID id) {
        return dsl.deleteFrom(ISSUE).where(ISSUE.ID.eq(id)).execute();
    }

    public PageDto<Issue> listForProject(UUID projectId, IssueListQuery query) {
        var conditions = new ArrayList<Condition>();
        conditions.add(ISSUE.PROJECT_ID.eq(projectId));
        if (query.statuses() != null && !query.statuses().isEmpty()) {
            conditions.add(ISSUE.STATUS.in(query.statuses()));
        }
        if (query.priorities() != null && !query.priorities().isEmpty()) {
            conditions.add(ISSUE.PRIORITY.in(query.priorities()));
        }
        if (query.labelIds() != null && !query.labelIds().isEmpty()) {
            conditions.add(ISSUE.ID.in(
                dsl.select(ISSUE_LABEL.ISSUE_ID).from(ISSUE_LABEL).where(ISSUE_LABEL.LABEL_ID.in(query.labelIds()))
            ));
        }
        if (query.q() != null) {
            var issueMatches = DSL.condition("{0} @@ websearch_to_tsquery('english', {1})", ISSUE.SEARCH_TSV, DSL.val(query.q()));
            var commentMatches = DSL.exists(
                dsl.selectOne().from(DSL.table("comment"))
                    .where(DSL.field("comment.issue_id").eq(ISSUE.ID)
                        .and(DSL.condition("{0} @@ websearch_to_tsquery('english', {1})",
                            DSL.field("comment.search_tsv"), DSL.val(query.q()))))
            );
            conditions.add(issueMatches.or(commentMatches));
        }
        Condition where = DSL.and(conditions);

        int total = dsl.fetchCount(dsl.selectFrom(ISSUE).where(where));
        List<Issue> items = dsl.selectFrom(ISSUE)
            .where(where)
            .orderBy(ISSUE.NUMBER.desc())
            .limit(query.size())
            .offset(query.page() * query.size())
            .fetchInto(Issue.class);
        return new PageDto<>(items, total, query.page(), query.size());
    }

    public Map<UUID, List<Label>> loadLabelsByIssueIds(Collection<UUID> issueIds) {
        if (issueIds == null || issueIds.isEmpty()) return Map.of();
        org.jooq.Field<?>[] labelFields = LABEL.fields();
        org.jooq.Field<?>[] selected = new org.jooq.Field<?>[labelFields.length + 1];
        selected[0] = ISSUE_LABEL.ISSUE_ID;
        System.arraycopy(labelFields, 0, selected, 1, labelFields.length);
        return dsl.select(selected)
            .from(ISSUE_LABEL)
            .join(LABEL).on(LABEL.ID.eq(ISSUE_LABEL.LABEL_ID))
            .where(ISSUE_LABEL.ISSUE_ID.in(issueIds))
            .orderBy(LABEL.NAME.asc())
            .fetchGroups(ISSUE_LABEL.ISSUE_ID, r -> r.into(LABEL).into(Label.class));
    }
}
