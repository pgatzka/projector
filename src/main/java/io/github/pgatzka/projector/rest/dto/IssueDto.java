package io.github.pgatzka.projector.rest.dto;

import io.github.pgatzka.projector.jooq.enums.IssuePriority;
import io.github.pgatzka.projector.jooq.enums.IssueStatus;
import io.github.pgatzka.projector.jooq.tables.pojos.Issue;
import io.github.pgatzka.projector.jooq.tables.pojos.Label;
import io.github.pgatzka.projector.jooq.tables.pojos.Project;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record IssueDto(
    UUID id,
    UUID projectId,
    String projectKey,
    int number,
    String identifier,
    String title,
    String descriptionMd,
    IssueStatus status,
    IssuePriority priority,
    LocalDate dueDate,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    List<LabelDto> labels
) {
    public static IssueDto of(Issue issue, Project project, List<Label> labels) {
        return new IssueDto(
            issue.getId(),
            project.getId(),
            project.getKey(),
            issue.getNumber(),
            project.getKey() + "-" + issue.getNumber(),
            issue.getTitle(),
            issue.getDescriptionMd(),
            issue.getStatus(),
            issue.getPriority(),
            issue.getDueDate(),
            issue.getCreatedAt(),
            issue.getUpdatedAt(),
            labels.stream().map(LabelDto::of).toList()
        );
    }

    public static IssueDto of(Issue issue, Project project) {
        return of(issue, project, List.of());
    }
}
