package io.github.pgatzka.projector.web;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import io.github.pgatzka.projector.layout.LayoutEngine;
import io.github.pgatzka.projector.parser.DiagramParser;
import io.github.pgatzka.projector.render.RenderService;
import io.github.pgatzka.projector.svg.SvgRenderer;

/**
 * Web-layer test wiring the real render pipeline (no Mongo, no Spring context for the data
 * layer) so a YAML POST exercises parse + layout + SVG end to end.
 */
@WebMvcTest(RenderController.class)
@Import({ RenderService.class, DiagramParser.class, LayoutEngine.class, SvgRenderer.class,
		DiagramExceptionHandler.class })
class RenderControllerTest {

	@Autowired
	private MockMvc mvc;

	@Test
	void validYamlReturnsSvg() throws Exception {
		mvc.perform(post("/api/render").contentType(MediaType.TEXT_PLAIN).content(sample()))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith("image/svg+xml"))
				.andExpect(content().string(containsString("<svg")))
				.andExpect(content().string(containsString("In stock?")));
	}

	@Test
	void invalidDiagramReturns422WithErrors() throws Exception {
		String invalid = """
				nodes:
				  - { id: a, type: banana }
				edges:
				  - { from: a, to: ghost }
				""";

		mvc.perform(post("/api/render").contentType(MediaType.TEXT_PLAIN).content(invalid))
				.andExpect(status().isUnprocessableContent())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.errors").isArray())
				.andExpect(jsonPath("$.errors[0].message", notNullValue()));
	}

	@Test
	void malformedYamlReturns422WithLocation() throws Exception {
		mvc.perform(post("/api/render").contentType(MediaType.TEXT_PLAIN).content("nodes: [ {{{ broken"))
				.andExpect(status().isUnprocessableContent())
				.andExpect(jsonPath("$.errors[0].message", containsString("YAML syntax error")))
				.andExpect(jsonPath("$.errors[0].line", notNullValue()));
	}

	@Test
	void emptyBodyReturns422() throws Exception {
		mvc.perform(post("/api/render").contentType(MediaType.TEXT_PLAIN).content(""))
				.andExpect(status().isUnprocessableContent())
				.andExpect(jsonPath("$.errors[0].message", containsString("empty")));
	}

	private String sample() throws IOException {
		try (InputStream in = getClass().getResourceAsStream("/diagrams/order-processing.yaml")) {
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
	}
}
