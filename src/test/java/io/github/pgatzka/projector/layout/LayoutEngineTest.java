package io.github.pgatzka.projector.layout;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import io.github.pgatzka.projector.model.Diagram;
import io.github.pgatzka.projector.model.NodeType;
import io.github.pgatzka.projector.parser.DiagramParser;

class LayoutEngineTest {

	private final DiagramParser parser = new DiagramParser();
	private final LayoutEngine engine = new LayoutEngine();

	@Test
	void laysOutTheSampleDiagram() throws IOException {
		LaidOutDiagram laid = engine.layout(sample());

		assertThat(laid.name()).isEqualTo("Order Processing");
		assertThat(laid.width()).isPositive();
		assertThat(laid.height()).isPositive();
		assertThat(laid.nodes()).hasSize(9);
		assertThat(laid.edges()).hasSize(10);
	}

	@Test
	void everyNodeHasPositiveSizeAndFitsInsideBounds() throws IOException {
		LaidOutDiagram laid = engine.layout(sample());

		for (LaidOutNode n : laid.nodes()) {
			assertThat(n.width()).as("width of %s", n.id()).isPositive();
			assertThat(n.height()).as("height of %s", n.id()).isPositive();
			assertThat(n.x()).as("x of %s", n.id()).isGreaterThanOrEqualTo(0);
			assertThat(n.y()).as("y of %s", n.id()).isGreaterThanOrEqualTo(0);
			assertThat(n.x() + n.width()).isLessThanOrEqualTo(laid.width() + 0.01);
			assertThat(n.y() + n.height()).isLessThanOrEqualTo(laid.height() + 0.01);
		}
	}

	@Test
	void nodesDoNotOverlap() throws IOException {
		LaidOutDiagram laid = engine.layout(sample());

		var nodes = laid.nodes();
		for (int i = 0; i < nodes.size(); i++) {
			for (int j = i + 1; j < nodes.size(); j++) {
				assertThat(overlap(nodes.get(i), nodes.get(j)))
						.as("nodes %s and %s overlap", nodes.get(i).id(), nodes.get(j).id())
						.isFalse();
			}
		}
	}

	@Test
	void everyEdgeIsRoutedWithAtLeastTwoPoints() throws IOException {
		LaidOutDiagram laid = engine.layout(sample());

		for (LaidOutEdge e : laid.edges()) {
			assertThat(e.points()).as("route of %s->%s", e.from(), e.to()).hasSizeGreaterThanOrEqualTo(2);
		}
	}

	@Test
	void flowsTopToBottom() throws IOException {
		LaidOutDiagram laid = engine.layout(sample());

		double startY = nodeOfType(laid, NodeType.START).y();
		double endY = laid.nodes().stream()
				.filter(n -> n.type() == NodeType.END)
				.mapToDouble(LaidOutNode::y)
				.max().orElseThrow();
		assertThat(startY).as("start should be above end").isLessThan(endY);
	}

	@Test
	void layoutIsDeterministic() throws IOException {
		Diagram diagram = sample();
		LaidOutDiagram a = engine.layout(diagram);
		LaidOutDiagram b = engine.layout(diagram);

		assertThat(b.width()).isEqualTo(a.width());
		assertThat(b.height()).isEqualTo(a.height());
		for (int i = 0; i < a.nodes().size(); i++) {
			assertThat(b.nodes().get(i)).isEqualTo(a.nodes().get(i));
		}
		assertThat(b.edges()).isEqualTo(a.edges());
	}

	private static boolean overlap(LaidOutNode a, LaidOutNode b) {
		return a.x() < b.x() + b.width()
				&& b.x() < a.x() + a.width()
				&& a.y() < b.y() + b.height()
				&& b.y() < a.y() + a.height();
	}

	private static LaidOutNode nodeOfType(LaidOutDiagram laid, NodeType type) {
		return laid.nodes().stream().filter(n -> n.type() == type).findFirst().orElseThrow();
	}

	private Diagram sample() throws IOException {
		try (InputStream in = getClass().getResourceAsStream("/diagrams/order-processing.yaml")) {
			assertThat(in).isNotNull();
			return parser.parse(new String(in.readAllBytes(), StandardCharsets.UTF_8));
		}
	}
}
