package io.github.pgatzka.projector.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

/** Single class with nullable comment-only / activity-only fields — Jackson serializes nulls out, keeping the wire shape clean while avoiding sealed-interface boilerplate. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TimelineEntryDto(
    String type,
    UUID id,
    OffsetDateTime createdAt,
    String createdBy,
    String bodyMd,
    String action,
    Map<String, Object> payload
) {
    public static TimelineEntryDto comment(CommentDto c) {
        return new TimelineEntryDto("comment", c.id(), c.createdAt(), c.createdBy(), c.bodyMd(), null, null);
    }

    public static TimelineEntryDto activity(ActivityDto a) {
        return new TimelineEntryDto("activity", a.id(), a.createdAt(), a.createdBy(), null, a.action(), a.payload());
    }
}
