package io.github.pgatzka.projector.model;

/**
 * A validated activity-diagram node.
 *
 * @param id    unique, non-blank identifier referenced by edges
 * @param type  the node kind
 * @param label optional human-readable text (may be null, e.g. for start/end/fork/join)
 */
public record Node(String id, NodeType type, String label) {
}
