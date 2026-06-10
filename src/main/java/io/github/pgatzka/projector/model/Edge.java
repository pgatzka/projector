package io.github.pgatzka.projector.model;

/**
 * A validated directed flow between two nodes.
 *
 * @param from  id of the source node (exists in the diagram)
 * @param to    id of the target node (exists in the diagram)
 * @param guard optional guard condition, typically on edges leaving a decision (may be null)
 */
public record Edge(String from, String to, String guard) {
}
