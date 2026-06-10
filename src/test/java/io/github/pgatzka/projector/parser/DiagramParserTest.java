package io.github.pgatzka.projector.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.pgatzka.projector.model.Diagram;
import io.github.pgatzka.projector.model.Edge;
import io.github.pgatzka.projector.model.Node;
import io.github.pgatzka.projector.model.NodeType;

class DiagramParserTest {

	private final DiagramParser parser = new DiagramParser();

	@Test
	void parsesTheSampleDiagram() throws IOException {
		Diagram diagram = parser.parse(resource("/diagrams/order-processing.yaml"));

		assertThat(diagram.name()).isEqualTo("Order Processing");
		assertThat(diagram.nodes()).hasSize(9);
		assertThat(diagram.edges()).hasSize(10);

		assertThat(diagram.nodes())
				.contains(new Node("start", NodeType.START, null))
				.contains(new Node("check", NodeType.DECISION, "In stock?"))
				.contains(new Node("fork1", NodeType.FORK, null))
				.contains(new Node("done", NodeType.END, null));

		// guard is captured on the decision's outgoing edge
		assertThat(diagram.edges())
				.contains(new Edge("check", "fork1", "in stock"))
				.contains(new Edge("check", "backorder", "out of stock"))
				.contains(new Edge("start", "receive", null));
	}

	@Test
	void typeMatchingIsCaseInsensitive() {
		Diagram diagram = parser.parse("""
				nodes:
				  - { id: s, type: START }
				  - { id: a, type: Action, label: Do it }
				  - { id: e, type: end }
				edges:
				  - { from: s, to: a }
				  - { from: a, to: e }
				""");

		assertThat(diagram.nodes())
				.extracting(Node::type)
				.containsExactly(NodeType.START, NodeType.ACTION, NodeType.END);
	}

	@Test
	void reportsMalformedYamlWithLocation() {
		// ':' inside an unquoted flow mapping is a YAML syntax error
		DiagramParseException ex = assertThrows(DiagramParseException.class, () ->
				parser.parse("nodes: [ {{{ broken"));

		assertThat(ex.errors()).hasSize(1);
		DiagramError error = ex.errors().getFirst();
		assertThat(error.message()).startsWith("YAML syntax error");
		assertThat(error.line()).isNotNull();
	}

	@Test
	void reportsUnknownNodeType() {
		DiagramParseException ex = assertThrows(DiagramParseException.class, () ->
				parser.parse("""
						nodes:
						  - { id: s, type: start }
						  - { id: x, type: banana }
						  - { id: e, type: end }
						edges:
						  - { from: s, to: e }
						"""));

		assertThat(ex.errors())
				.anyMatch(e -> e.message().contains("unknown type 'banana'"));
	}

	@Test
	void reportsDanglingEdge() {
		DiagramParseException ex = assertThrows(DiagramParseException.class, () ->
				parser.parse("""
						nodes:
						  - { id: s, type: start }
						  - { id: e, type: end }
						edges:
						  - { from: s, to: nowhere }
						"""));

		assertThat(ex.errors())
				.anyMatch(e -> e.message().contains("'to' references unknown node 'nowhere'"));
	}

	@Test
	void reportsDuplicateId() {
		DiagramParseException ex = assertThrows(DiagramParseException.class, () ->
				parser.parse("""
						nodes:
						  - { id: s, type: start }
						  - { id: s, type: action, label: dup }
						  - { id: e, type: end }
						edges:
						  - { from: s, to: e }
						"""));

		assertThat(ex.errors())
				.anyMatch(e -> e.message().contains("duplicate node id 's'"));
	}

	@Test
	void requiresExactlyOneStart() {
		DiagramParseException noStart = assertThrows(DiagramParseException.class, () ->
				parser.parse("""
						nodes:
						  - { id: a, type: action, label: x }
						  - { id: e, type: end }
						edges:
						  - { from: a, to: e }
						"""));
		assertThat(noStart.errors()).anyMatch(e -> e.message().contains("exactly one start node (found 0)"));

		DiagramParseException twoStarts = assertThrows(DiagramParseException.class, () ->
				parser.parse("""
						nodes:
						  - { id: s1, type: start }
						  - { id: s2, type: start }
						  - { id: e, type: end }
						edges:
						  - { from: s1, to: e }
						  - { from: s2, to: e }
						"""));
		assertThat(twoStarts.errors()).anyMatch(e -> e.message().contains("exactly one start node (found 2)"));
	}

	@Test
	void requiresAtLeastOneEnd() {
		DiagramParseException ex = assertThrows(DiagramParseException.class, () ->
				parser.parse("""
						nodes:
						  - { id: s, type: start }
						  - { id: a, type: action, label: x }
						edges:
						  - { from: s, to: a }
						"""));

		assertThat(ex.errors()).anyMatch(e -> e.message().contains("at least one end node"));
	}

	@Test
	void collectsMultipleErrorsAtOnce() {
		DiagramParseException ex = assertThrows(DiagramParseException.class, () ->
				parser.parse("""
						nodes:
						  - { id: a, type: banana }
						edges:
						  - { from: a, to: ghost }
						"""));

		List<DiagramError> errors = ex.errors();
		// unknown type + no start + no end + dangling 'to'  => several at once
		assertThat(errors.size()).isGreaterThanOrEqualTo(3);
	}

	@Test
	void rejectsEmptyInput() {
		DiagramParseException ex = assertThrows(DiagramParseException.class, () -> parser.parse("   "));
		assertThat(ex.errors()).anyMatch(e -> e.message().contains("empty"));
	}

	private String resource(String path) throws IOException {
		try (InputStream in = getClass().getResourceAsStream(path)) {
			assertThat(in).as("test resource %s", path).isNotNull();
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
	}
}
