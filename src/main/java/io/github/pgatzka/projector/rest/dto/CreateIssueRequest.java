package io.github.pgatzka.projector.rest.dto;

import io.github.pgatzka.projector.jooq.enums.IssuePriority;
import io.github.pgatzka.projector.jooq.enums.IssueStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreateIssueRequest(
    @NotBlank @Size(max = 200) String title,
    @Size(max = 50000) String descriptionMd,
    IssueStatus status,
    IssuePriority priority,
    LocalDate dueDate,
    List<UUID> labelIds
) {}
