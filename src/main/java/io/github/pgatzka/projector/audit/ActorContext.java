package io.github.pgatzka.projector.audit;

import org.springframework.stereotype.Component;

/**
 * Returns the current actor as the canonical "user:<uuid>" string used in
 * the created_by / updated_by audit columns.
 *
 * v0.1: no inserts/updates happen yet, so calling currentActor() throws.
 * v0.2 will replace this with a SecurityContextHolder-backed implementation.
 */
@Component
public class ActorContext {

    public String currentActor() {
        throw new IllegalStateException(
            "ActorContext.currentActor() called in v0.1 — no authenticated principal yet. "
            + "This will be implemented in v0.2 once auth lands."
        );
    }
}
