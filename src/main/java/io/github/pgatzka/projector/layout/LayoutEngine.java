package io.github.pgatzka.projector.layout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.elk.alg.layered.options.LayeredMetaDataProvider;
import org.eclipse.elk.core.RecursiveGraphLayoutEngine;
import org.eclipse.elk.core.data.LayoutMetaDataService;
import org.eclipse.elk.core.options.CoreOptions;
import org.eclipse.elk.core.options.Direction;
import org.eclipse.elk.core.options.EdgeRouting;
import org.eclipse.elk.core.util.NullElkProgressMonitor;
import org.eclipse.elk.graph.ElkBendPoint;
import org.eclipse.elk.graph.ElkEdge;
import org.eclipse.elk.graph.ElkEdgeSection;
import org.eclipse.elk.graph.ElkNode;
import org.eclipse.elk.graph.util.ElkGraphUtil;
import org.springframework.stereotype.Component;

import io.github.pgatzka.projector.model.Diagram;
import io.github.pgatzka.projector.model.Edge;
import io.github.pgatzka.projector.model.Node;

/**
 * Lays out a validated {@link Diagram} top-to-bottom using ELK's {@code layered} algorithm
 * with orthogonal edge routing, producing absolute coordinates for nodes and edges.
 */
@Component
public class LayoutEngine {

	private static final double NODE_SPACING = 40;
	private static final double LAYER_SPACING = 45;

	static {
		// Register the layered algorithm with ELK's metadata service (needed outside OSGi).
		LayoutMetaDataService.getInstance().registerLayoutMetaDataProviders(new LayeredMetaDataProvider());
	}

	public LaidOutDiagram layout(Diagram diagram) {
		ElkNode graph = ElkGraphUtil.createGraph();
		graph.setProperty(CoreOptions.ALGORITHM, "org.eclipse.elk.layered");
		graph.setProperty(CoreOptions.DIRECTION, Direction.DOWN);
		graph.setProperty(CoreOptions.EDGE_ROUTING, EdgeRouting.ORTHOGONAL);
		graph.setProperty(CoreOptions.SPACING_NODE_NODE, NODE_SPACING);
		graph.setProperty(CoreOptions.SPACING_EDGE_NODE, 20.0);

		Map<String, ElkNode> elkNodes = new HashMap<>();
		for (Node node : diagram.nodes()) {
			ElkNode elk = ElkGraphUtil.createNode(graph);
			elk.setIdentifier(node.id());
			double[] size = NodeSizer.size(node);
			elk.setDimensions(size[0], size[1]);
			elkNodes.put(node.id(), elk);
		}

		List<ElkEdge> elkEdges = new ArrayList<>();
		for (Edge edge : diagram.edges()) {
			ElkEdge elk = ElkGraphUtil.createSimpleEdge(elkNodes.get(edge.from()), elkNodes.get(edge.to()));
			elkEdges.add(elk);
		}

		new RecursiveGraphLayoutEngine().layout(graph, new NullElkProgressMonitor());

		return new LaidOutDiagram(diagram.name(), graph.getWidth(), graph.getHeight(),
				extractNodes(diagram, elkNodes), extractEdges(diagram, elkEdges));
	}

	private List<LaidOutNode> extractNodes(Diagram diagram, Map<String, ElkNode> elkNodes) {
		List<LaidOutNode> result = new ArrayList<>();
		for (Node node : diagram.nodes()) {
			ElkNode elk = elkNodes.get(node.id());
			result.add(new LaidOutNode(node.id(), node.type(), node.label(),
					elk.getX(), elk.getY(), elk.getWidth(), elk.getHeight()));
		}
		return result;
	}

	private List<LaidOutEdge> extractEdges(Diagram diagram, List<ElkEdge> elkEdges) {
		List<LaidOutEdge> result = new ArrayList<>();
		for (int i = 0; i < diagram.edges().size(); i++) {
			Edge edge = diagram.edges().get(i);
			result.add(new LaidOutEdge(edge.from(), edge.to(), edge.guard(), route(elkEdges.get(i))));
		}
		return result;
	}

	private List<Point> route(ElkEdge elk) {
		List<Point> points = new ArrayList<>();
		if (!elk.getSections().isEmpty()) {
			ElkEdgeSection section = elk.getSections().getFirst();
			points.add(new Point(section.getStartX(), section.getStartY()));
			for (ElkBendPoint bend : section.getBendPoints()) {
				points.add(new Point(bend.getX(), bend.getY()));
			}
			points.add(new Point(section.getEndX(), section.getEndY()));
		}
		return points;
	}
}
