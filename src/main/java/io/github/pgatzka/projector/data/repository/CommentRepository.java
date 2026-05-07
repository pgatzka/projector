package io.github.pgatzka.projector.data.repository;

import io.github.pgatzka.projector.jooq.tables.pojos.Comment;
import io.github.pgatzka.projector.jooq.tables.records.CommentRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

import static io.github.pgatzka.projector.jooq.tables.Comment.COMMENT;

@Repository
public class CommentRepository {

    private final DSLContext dsl;

    public CommentRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public Comment insert(Comment comment) {
        CommentRecord record = dsl.newRecord(COMMENT, comment);
        record.changed(COMMENT.SEARCH_TSV, false);
        record.store();
        record.refresh();
        return record.into(Comment.class);
    }

    public List<Comment> findByIssueIdOrderByCreatedAtAsc(UUID issueId) {
        return dsl.selectFrom(COMMENT)
            .where(COMMENT.ISSUE_ID.eq(issueId))
            .orderBy(COMMENT.CREATED_AT.asc())
            .fetchInto(Comment.class);
    }
}
