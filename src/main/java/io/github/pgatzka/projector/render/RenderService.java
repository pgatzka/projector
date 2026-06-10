package io.github.pgatzka.projector.render;

import org.springframework.stereotype.Service;

import io.github.pgatzka.projector.layout.LayoutEngine;
import io.github.pgatzka.projector.model.Diagram;
import io.github.pgatzka.projector.parser.DiagramParser;
import io.github.pgatzka.projector.svg.SvgRenderer;

/**
 * Orchestrates the full render pipeline: parse YAML, lay out, and render to SVG.
 * Throws {@link io.github.pgatzka.projector.parser.DiagramParseException} if the YAML is
 * invalid.
 */
@Service
public class RenderService {

	private final DiagramParser parser;
	private final LayoutEngine layoutEngine;
	private final SvgRenderer svgRenderer;

	public RenderService(DiagramParser parser, LayoutEngine layoutEngine, SvgRenderer svgRenderer) {
		this.parser = parser;
		this.layoutEngine = layoutEngine;
		this.svgRenderer = svgRenderer;
	}

	/** Parses, lays out, and renders the given YAML to an SVG document. */
	public String renderSvg(String yaml) {
		Diagram diagram = parser.parse(yaml);
		return svgRenderer.render(layoutEngine.layout(diagram));
	}
}
