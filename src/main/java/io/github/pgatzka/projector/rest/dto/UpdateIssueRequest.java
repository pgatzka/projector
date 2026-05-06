package io.github.pgatzka.projector.rest.dto;

import io.github.pgatzka.projector.jooq.enums.IssuePriority;
import io.github.pgatzka.projector.jooq.enums.IssueStatus;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateIssueRequest(
    @Size(min = 1, max = 200) String title,
    @Size(max = 50000) String descriptionMd,
    IssueStatus status,
    IssuePriority priority,
    LocalDate dueDate
) {}
