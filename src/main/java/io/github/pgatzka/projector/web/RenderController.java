package io.github.pgatzka.projector.web;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.github.pgatzka.projector.render.RenderService;

/**
 * Renders a YAML activity diagram to SVG.
 *
 * <p>{@code POST /api/render} with a {@code text/plain} YAML body returns {@code 200} with
 * {@code image/svg+xml}. Invalid input yields {@code 422} with a JSON error body (see
 * {@link DiagramExceptionHandler}).
 */
@RestController
public class RenderController {

	private final RenderService renderService;

	public RenderController(RenderService renderService) {
		this.renderService = renderService;
	}

	@PostMapping(value = "/api/render", produces = "image/svg+xml")
	public String render(@RequestBody(required = false) String yaml) {
		return renderService.renderSvg(yaml);
	}
}
