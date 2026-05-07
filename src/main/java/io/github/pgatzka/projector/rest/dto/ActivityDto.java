package io.github.pgatzka.projector.rest.dto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.pgatzka.projector.jooq.tables.pojos.Activity;
import org.jooq.JSONB;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public record ActivityDto(
    UUID id,
    OffsetDateTime createdAt,
    String createdBy,
    String action,
    Map<String, Object> payload
) {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    public static ActivityDto of(Activity a) {
        return new ActivityDto(
            a.getId(),
            a.getCreatedAt(),
            a.getCreatedBy(),
            a.getAction() == null ? null : a.getAction().getLiteral(),
            parsePayload(a.getPayload())
        );
    }

    private static Map<String, Object> parsePayload(JSONB payload) {
        if (payload == null) return new LinkedHashMap<>();
        String s = payload.data();
        if (s == null || s.isBlank()) return new LinkedHashMap<>();
        try {
            return MAPPER.readValue(s, MAP_TYPE);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize activity payload", e);
        }
    }
}
