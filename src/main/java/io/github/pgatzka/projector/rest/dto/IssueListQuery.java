package io.github.pgatzka.projector.rest.dto;

import io.github.pgatzka.projector.jooq.enums.IssuePriority;
import io.github.pgatzka.projector.jooq.enums.IssueStatus;

import java.util.List;
import java.util.UUID;

public record IssueListQuery(
    List<IssueStatus> statuses,
    List<IssuePriority> priorities,
    List<UUID> labelIds,
    String q,
    int page,
    int size
) {
    public static IssueListQuery of(List<IssueStatus> s, List<IssuePriority> p, List<UUID> l, String q, Integer page, Integer size) {
        int pg = (page == null || page < 0) ? 0 : page;
        int sz = (size == null || size < 1) ? 50 : Math.min(size, 100);
        String qq = (q == null || q.isBlank()) ? null : q;
        return new IssueListQuery(s, p, l, qq, pg, sz);
    }
}
