package io.github.pgatzka.projector.audit;

/**
 * Thread-local override used during /api/setup, where the new admin account's
 * UUID is known before the row is inserted. SetupService sets the override
 * before calling AccountDataService.create(...) so AuditRecordListener can
 * read the actor string from the override.
 */
public final class SetupActorOverride {

    private static final ThreadLocal<String> ACTOR = new ThreadLocal<>();

    private SetupActorOverride() {}

    public static void set(String actor) {
        ACTOR.set(actor);
    }

    public static String get() {
        return ACTOR.get();
    }

    public static void clear() {
        ACTOR.remove();
    }
}
