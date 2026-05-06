package io.github.pgatzka.projector.audit;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * Returns the current actor as the canonical "user:<uuid>" string used in
 * the created_by / updated_by audit columns.
 *
 * Resolution order:
 *   1. SetupActorOverride (used during /api/setup before the principal exists)
 *   2. SecurityContextHolder authentication principal (the normal case)
 *   3. system() — only callable from code that explicitly opts in
 */
@Component
public class ActorContext {

    public String currentActor() {
        String override = SetupActorOverride.get();
        if (override != null) return override;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            throw new IllegalStateException(
                "ActorContext.currentActor() called with no authenticated principal "
                + "and no SetupActorOverride. Either authenticate first or use ActorContext.system()."
            );
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof AccountPrincipal accountPrincipal) {
            return "user:" + accountPrincipal.id();
        }
        if (principal instanceof UserDetails userDetails) {
            return "user:" + userDetails.getUsername();
        }
        throw new IllegalStateException(
            "Unexpected principal type: " + principal.getClass().getName()
        );
    }

    public String system() {
        return "system:bootstrap";
    }
}
