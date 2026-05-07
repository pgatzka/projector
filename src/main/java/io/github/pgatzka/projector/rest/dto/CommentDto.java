package io.github.pgatzka.projector.rest.dto;

import io.github.pgatzka.projector.jooq.tables.pojos.Comment;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CommentDto(
    UUID id,
    OffsetDateTime createdAt,
    String createdBy,
    String bodyMd
) {
    public static CommentDto of(Comment c) {
        return new CommentDto(c.getId(), c.getCreatedAt(), c.getCreatedBy(), c.getBodyMd());
    }
}
