package io.github.pgatzka.projector.model;

import java.util.List;

/**
 * A validated activity diagram: a name plus its nodes and edges. Instances are only
 * produced after validation succeeds, so all edges reference existing nodes, ids are
 * unique, and the start/end constraints hold.
 *
 * @param name  optional diagram name (may be null)
 * @param nodes the diagram's nodes
 * @param edges the directed flows between nodes
 */
public record Diagram(String name, List<Node> nodes, List<Edge> edges) {

	public Diagram {
		nodes = List.copyOf(nodes);
		edges = List.copyOf(edges);
	}
}
