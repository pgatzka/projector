package io.github.pgatzka.projector.svg;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.jupiter.api.Test;

import io.github.pgatzka.projector.layout.LaidOutDiagram;
import io.github.pgatzka.projector.layout.LaidOutNode;
import io.github.pgatzka.projector.layout.LayoutEngine;
import io.github.pgatzka.projector.model.NodeType;
import io.github.pgatzka.projector.parser.DiagramParser;

class SvgRendererTest {

	private final DiagramParser parser = new DiagramParser();
	private final LayoutEngine engine = new LayoutEngine();
	private final SvgRenderer renderer = new SvgRenderer();

	@Test
	void rendersWellFormedSvgForTheSample() throws IOException {
		String svg = renderSample();

		assertThat(svg).startsWith("<svg xmlns=\"http://www.w3.org/2000/svg\"");
		assertThat(svg).contains("viewBox=\"0 0 ");
		assertThat(svg).contains("<style>");
		assertThat(svg).contains("<marker id=\"pj-arrow\"");
		assertParsesAsXml(svg);
	}

	@Test
	void emitsTheExpectedShapesForEachNodeType() throws IOException {
		String svg = renderSample();

		assertThat(count(svg, "class=\"pj-edge\"")).as("edges").isEqualTo(10);
		assertThat(count(svg, "class=\"pj-action\"")).as("action boxes").isEqualTo(4);
		assertThat(count(svg, "class=\"pj-decision\"")).as("decision diamonds").isEqualTo(1);
		assertThat(count(svg, "class=\"pj-bar\"")).as("fork/join bars").isEqualTo(2);
		assertThat(count(svg, "class=\"pj-terminal\"")).as("start").isEqualTo(1);
		assertThat(count(svg, "class=\"pj-end-ring\"")).as("end").isEqualTo(1);
		assertThat(svg).contains("marker-end=\"url(#pj-arrow)\"");
	}

	@Test
	void includesNodeLabelsAndEdgeGuards() throws IOException {
		String svg = renderSample();

		assertThat(svg).contains("Receive order");
		assertThat(svg).contains("In stock?");
		assertThat(svg).contains("in stock");
		assertThat(svg).contains("out of stock");
	}

	@Test
	void escapesXmlSpecialCharactersInText() {
		LaidOutDiagram diagram = new LaidOutDiagram("t", 120, 60,
				List.of(new LaidOutNode("a", NodeType.ACTION, "A & B <x> \"q\" 'z'", 10, 10, 100, 40)),
				List.of());

		String svg = renderer.render(diagram);

		assertThat(svg).contains("A &amp; B &lt;x&gt; &quot;q&quot; &#39;z&#39;");
		assertThat(svg).doesNotContain("<x>");
		assertParsesAsXml(svg);
	}

	private String renderSample() throws IOException {
		try (InputStream in = getClass().getResourceAsStream("/diagrams/order-processing.yaml")) {
			assertThat(in).isNotNull();
			String yaml = new String(in.readAllBytes(), StandardCharsets.UTF_8);
			return renderer.render(engine.layout(parser.parse(yaml)));
		}
	}

	private static int count(String haystack, String needle) {
		int n = 0;
		for (int i = haystack.indexOf(needle); i >= 0; i = haystack.indexOf(needle, i + needle.length())) {
			n++;
		}
		return n;
	}

	private static void assertParsesAsXml(String svg) {
		assertThatCode(() -> {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			factory.newDocumentBuilder().parse(new ByteArrayInputStream(svg.getBytes(StandardCharsets.UTF_8)));
		}).as("SVG should be well-formed XML").doesNotThrowAnyException();
	}
}
