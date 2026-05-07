package io.github.pgatzka.projector.rest.dto;

import io.github.pgatzka.projector.jooq.enums.LabelColor;
import io.github.pgatzka.projector.jooq.tables.pojos.Label;

import java.time.OffsetDateTime;
import java.util.UUID;

public record LabelDto(
    UUID id,
    UUID projectId,
    String name,
    LabelColor color,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
    public static LabelDto of(Label l) {
        return new LabelDto(
            l.getId(), l.getProjectId(), l.getName(), l.getColor(),
            l.getCreatedAt(), l.getUpdatedAt()
        );
    }
}
