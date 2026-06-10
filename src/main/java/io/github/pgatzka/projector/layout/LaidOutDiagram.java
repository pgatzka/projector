package io.github.pgatzka.projector.layout;

import java.util.List;

/**
 * The result of laying out a {@link io.github.pgatzka.projector.model.Diagram}: positioned
 * nodes and routed edges, plus the overall drawing bounds (used for the SVG viewBox).
 */
public record LaidOutDiagram(String name, double width, double height,
		List<LaidOutNode> nodes, List<LaidOutEdge> edges) {

	public LaidOutDiagram {
		nodes = List.copyOf(nodes);
		edges = List.copyOf(edges);
	}
}
