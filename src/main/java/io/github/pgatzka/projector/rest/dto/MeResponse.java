package io.github.pgatzka.projector.rest.dto;

import io.github.pgatzka.projector.jooq.tables.pojos.Account;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MeResponse(
    UUID id,
    String email,
    String displayName,
    OffsetDateTime lastLoginAt
) {
    public static MeResponse of(Account account) {
        return new MeResponse(
            account.getId(),
            account.getEmail(),
            account.getDisplayName(),
            account.getLastLoginAt()
        );
    }
}
