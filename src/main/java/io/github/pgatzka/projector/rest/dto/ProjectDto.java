package io.github.pgatzka.projector.rest.dto;

import io.github.pgatzka.projector.jooq.tables.pojos.Project;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ProjectDto(
    UUID id,
    String key,
    String name,
    String description,
    int nextIssueNumber,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
    public static ProjectDto of(Project p) {
        return new ProjectDto(
            p.getId(), p.getKey(), p.getName(), p.getDescription(),
            p.getNextIssueNumber(), p.getCreatedAt(), p.getUpdatedAt()
        );
    }
}
