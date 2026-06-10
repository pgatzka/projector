package io.github.pgatzka.projector.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;

import org.springframework.stereotype.Component;

import tools.jackson.core.JacksonException;
import tools.jackson.core.TokenStreamLocation;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.dataformat.yaml.YAMLMapper;

import io.github.pgatzka.projector.model.Diagram;
import io.github.pgatzka.projector.model.Edge;
import io.github.pgatzka.projector.model.Node;
import io.github.pgatzka.projector.model.NodeType;

/**
 * Parses the projector YAML DSL into a validated {@link Diagram}.
 *
 * <p>Parsing has two phases: lenient binding of the YAML into DTOs (syntax errors here
 * carry a line/column), then semantic validation. Validation is "moderate": exactly one
 * start node, at least one end node, unique ids, and every edge referencing an existing
 * node (reachability is not enforced). All problems are collected and reported together
 * via {@link DiagramParseException}.
 */
@Component
public class DiagramParser {

	private static final String VALID_TYPES = "start, end, action, decision, merge, fork, join";

	private final YAMLMapper mapper = YAMLMapper.builder()
			.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
			.build();

	public Diagram parse(String yaml) {
		return validateAndBuild(readYaml(yaml));
	}

	private DiagramDto readYaml(String yaml) {
		if (yaml == null || yaml.isBlank()) {
			throw new DiagramParseException(List.of(DiagramError.of("Diagram is empty")));
		}
		try {
			DiagramDto dto = mapper.readValue(yaml, DiagramDto.class);
			if (dto == null) {
				throw new DiagramParseException(List.of(DiagramError.of("Diagram is empty")));
			}
			return dto;
		} catch (JacksonException e) {
			throw new DiagramParseException(List.of(toSyntaxError(e)));
		}
	}

	private DiagramError toSyntaxError(JacksonException e) {
		String detail = e.getOriginalMessage() != null ? e.getOriginalMessage() : e.getMessage();
		String message = "YAML syntax error: " + detail;
		TokenStreamLocation loc = e.getLocation();
		if (loc != null && loc != TokenStreamLocation.NA && loc.getLineNr() > 0) {
			return DiagramError.at(message, loc.getLineNr(), loc.getColumnNr());
		}
		return DiagramError.of(message);
	}

	private Diagram validateAndBuild(DiagramDto dto) {
		List<DiagramError> errors = new ArrayList<>();
		List<NodeDto> nodeDtos = dto.nodes == null ? List.of() : dto.nodes;
		List<EdgeDto> edgeDtos = dto.edges == null ? List.of() : dto.edges;

		// --- nodes ---
		Set<String> declaredIds = new HashSet<>(); // every non-blank id, used for edge ref checks
		Set<String> seenIds = new HashSet<>();      // for duplicate detection
		List<Node> nodes = new ArrayList<>();
		int startCount = 0;
		int endCount = 0;

		for (int i = 0; i < nodeDtos.size(); i++) {
			NodeDto n = nodeDtos.get(i);
			String id = (n == null || n.id == null) ? null : n.id.trim();
			boolean idOk = id != null && !id.isEmpty();
			String where = idOk ? "node '" + id + "'" : "node[" + i + "]";

			if (!idOk) {
				errors.add(DiagramError.of(where + ": missing id"));
			} else {
				declaredIds.add(id);
				if (!seenIds.add(id)) {
					errors.add(DiagramError.of("duplicate node id '" + id + "'"));
				}
			}

			String rawType = (n == null) ? null : n.type;
			Optional<NodeType> type = NodeType.fromYaml(rawType);
			if (rawType == null || rawType.isBlank()) {
				errors.add(DiagramError.of(where + ": missing type"));
			} else if (type.isEmpty()) {
				errors.add(DiagramError.of(where + ": unknown type '" + rawType + "' (expected one of " + VALID_TYPES + ")"));
			}

			if (type.isPresent()) {
				if (type.get() == NodeType.START) {
					startCount++;
				} else if (type.get() == NodeType.END) {
					endCount++;
				}
			}

			if (idOk && type.isPresent()) {
				nodes.add(new Node(id, type.get(), normalize(n.label)));
			}
		}

		// --- moderate start/end constraints ---
		if (startCount != 1) {
			errors.add(DiagramError.of("a diagram must have exactly one start node (found " + startCount + ")"));
		}
		if (endCount == 0) {
			errors.add(DiagramError.of("a diagram must have at least one end node (found none)"));
		}

		// --- edges ---
		List<Edge> edges = new ArrayList<>();
		for (int j = 0; j < edgeDtos.size(); j++) {
			EdgeDto e = edgeDtos.get(j);
			String from = (e == null || e.from == null) ? null : e.from.trim();
			String to = (e == null || e.to == null) ? null : e.to.trim();
			String where = "edge[" + j + "]";
			boolean fromOk = checkEndpoint(where, "from", from, declaredIds, errors);
			boolean toOk = checkEndpoint(where, "to", to, declaredIds, errors);
			if (fromOk && toOk) {
				edges.add(new Edge(from, to, normalize(e.guard)));
			}
		}

		if (!errors.isEmpty()) {
			throw new DiagramParseException(errors);
		}

		return new Diagram(normalize(dto.diagram), nodes, edges);
	}

	/** Validates one edge endpoint, recording an error if blank or referencing an unknown node. */
	private boolean checkEndpoint(String where, String field, String value, Set<String> declaredIds, List<DiagramError> errors) {
		if (value == null || value.isEmpty()) {
			errors.add(DiagramError.of(where + ": missing '" + field + "'"));
			return false;
		}
		if (!declaredIds.contains(value)) {
			errors.add(DiagramError.of(where + ": '" + field + "' references unknown node '" + value + "'"));
			return false;
		}
		return true;
	}

	private static String normalize(String value) {
		return (value == null || value.isBlank()) ? null : value.trim();
	}

	// --- DTOs for lenient YAML binding (fields nullable so validation can report what's missing) ---

	static final class DiagramDto {
		public String diagram;
		public List<NodeDto> nodes;
		public List<EdgeDto> edges;
	}

	static final class NodeDto {
		public String id;
		public String type;
		public String label;
	}

	static final class EdgeDto {
		public String from;
		public String to;
		public String guard;
	}
}
