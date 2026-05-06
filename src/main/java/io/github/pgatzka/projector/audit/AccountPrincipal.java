package io.github.pgatzka.projector.audit;

import java.util.UUID;

/**
 * Lightweight principal carrying the account id and email, attached to the
 * Spring Security Authentication after a successful login. Avoids leaking the
 * jOOQ-generated Account POJO (with password_hash) into the security context.
 */
public record AccountPrincipal(UUID id, String email, String displayName) {}
