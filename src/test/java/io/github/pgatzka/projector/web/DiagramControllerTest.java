package io.github.pgatzka.projector.web;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import io.github.pgatzka.projector.store.DiagramDocument;
import io.github.pgatzka.projector.store.DiagramNotFoundException;
import io.github.pgatzka.projector.store.DiagramService;

@WebMvcTest(DiagramController.class)
@Import(DiagramExceptionHandler.class)
class DiagramControllerTest {

	@Autowired
	private MockMvc mvc;

	@MockitoBean
	private DiagramService service;

	@Test
	void createReturns201WithLocationAndBody() throws Exception {
		when(service.create(eq("Flow"), anyString())).thenReturn(doc("abc", "Flow", "x: 1"));

		mvc.perform(post("/api/diagrams").contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"Flow\",\"yaml\":\"x: 1\"}"))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "/api/diagrams/abc"))
				.andExpect(jsonPath("$.id").value("abc"))
				.andExpect(jsonPath("$.name").value("Flow"))
				.andExpect(jsonPath("$.yaml").value("x: 1"))
				// timestamps serialize as ISO-8601 strings, not epoch numbers
				.andExpect(jsonPath("$.createdAt").value("2026-01-01T00:00:00Z"));
	}

	@Test
	void createWithBlankNameReturns400() throws Exception {
		mvc.perform(post("/api/diagrams").contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"  \",\"yaml\":\"x: 1\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("'name' is required"));
	}

	@Test
	void listReturnsSummariesWithoutYaml() throws Exception {
		when(service.findAll()).thenReturn(java.util.List.of(doc("a", "One", "y1"), doc("b", "Two", "y2")));

		mvc.perform(get("/api/diagrams"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value("a"))
				.andExpect(jsonPath("$[0].name").value("One"))
				.andExpect(jsonPath("$[0].yaml").doesNotExist())
				.andExpect(jsonPath("$[1].id").value("b"));
	}

	@Test
	void getReturnsDiagramOr404() throws Exception {
		when(service.get("abc")).thenReturn(doc("abc", "Flow", "x: 1"));
		when(service.get("missing")).thenThrow(new DiagramNotFoundException("missing"));

		mvc.perform(get("/api/diagrams/abc")).andExpect(status().isOk()).andExpect(jsonPath("$.yaml").value("x: 1"));
		mvc.perform(get("/api/diagrams/missing")).andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Diagram not found: missing"));
	}

	@Test
	void updateReturnsDiagramOr404() throws Exception {
		when(service.update(eq("abc"), eq("New"), anyString())).thenReturn(doc("abc", "New", "y: 2"));
		when(service.update(eq("missing"), anyString(), anyString())).thenThrow(new DiagramNotFoundException("missing"));

		mvc.perform(put("/api/diagrams/abc").contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"New\",\"yaml\":\"y: 2\"}"))
				.andExpect(status().isOk()).andExpect(jsonPath("$.name").value("New"));
		mvc.perform(put("/api/diagrams/missing").contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"New\",\"yaml\":\"y: 2\"}"))
				.andExpect(status().isNotFound());
	}

	@Test
	void deleteReturns204Or404() throws Exception {
		mvc.perform(delete("/api/diagrams/abc")).andExpect(status().isNoContent()).andExpect(content().string(""));

		doThrow(new DiagramNotFoundException("missing")).when(service).delete("missing");
		mvc.perform(delete("/api/diagrams/missing")).andExpect(status().isNotFound());
	}

	private static DiagramDocument doc(String id, String name, String yaml) {
		DiagramDocument d = new DiagramDocument(name, yaml);
		ReflectionTestUtils.setField(d, "id", id);
		ReflectionTestUtils.setField(d, "createdAt", Instant.parse("2026-01-01T00:00:00Z"));
		ReflectionTestUtils.setField(d, "updatedAt", Instant.parse("2026-01-01T00:00:00Z"));
		return d;
	}
}
