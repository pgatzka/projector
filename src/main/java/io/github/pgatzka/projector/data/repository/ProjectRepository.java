package io.github.pgatzka.projector.data.repository;

import io.github.pgatzka.projector.jooq.tables.pojos.Project;
import io.github.pgatzka.projector.jooq.tables.records.ProjectRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static io.github.pgatzka.projector.jooq.tables.Project.PROJECT;

@Repository
public class ProjectRepository {

    private final DSLContext dsl;

    public ProjectRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public List<Project> findAllOrderByKey() {
        return dsl.selectFrom(PROJECT)
            .orderBy(PROJECT.KEY.asc())
            .fetchInto(Project.class);
    }

    public Optional<Project> findById(UUID id) {
        return dsl.selectFrom(PROJECT).where(PROJECT.ID.eq(id)).fetchOptionalInto(Project.class);
    }

    public Optional<Project> findByKey(String key) {
        return dsl.selectFrom(PROJECT).where(PROJECT.KEY.eq(key)).fetchOptionalInto(Project.class);
    }

    public boolean existsByKey(String key) {
        return dsl.fetchExists(PROJECT, PROJECT.KEY.eq(key));
    }

    public Project insert(Project project) {
        ProjectRecord record = dsl.newRecord(PROJECT, project);
        record.store();
        return record.into(Project.class);
    }

    public Project update(Project project) {
        ProjectRecord record = dsl.newRecord(PROJECT, project);
        record.update();
        return record.into(Project.class);
    }

    public int deleteById(UUID id) {
        return dsl.deleteFrom(PROJECT).where(PROJECT.ID.eq(id)).execute();
    }

    /**
     * Locks the project row, increments next_issue_number, returns the value
     * to claim for a new issue. Must be called inside a transaction.
     */
    public int claimNextIssueNumber(UUID projectId) {
        Integer current = dsl.select(PROJECT.NEXT_ISSUE_NUMBER)
            .from(PROJECT)
            .where(PROJECT.ID.eq(projectId))
            .forUpdate()
            .fetchOne(PROJECT.NEXT_ISSUE_NUMBER);
        if (current == null) {
            throw new IllegalStateException("Project " + projectId + " not found while claiming issue number");
        }
        dsl.update(PROJECT)
            .set(PROJECT.NEXT_ISSUE_NUMBER, current + 1)
            .where(PROJECT.ID.eq(projectId))
            .execute();
        return current;
    }
}
