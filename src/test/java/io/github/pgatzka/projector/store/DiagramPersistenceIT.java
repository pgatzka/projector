package io.github.pgatzka.projector.store;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Persistence integration test against a real MongoDB (started by docker-maven-plugin in
 * the integration-test phase on the fixed host port 27018). Exercises the full CRUD path
 * and auditing timestamps.
 */
@DataMongoTest
@Import({ DiagramService.class, MongoConfig.class })
class DiagramPersistenceIT {

	@DynamicPropertySource
	static void mongoProperties(DynamicPropertyRegistry registry) {
		// Spring Boot 4 moved Mongo connection properties to the "spring.mongodb" prefix
		// (no longer "spring.data.mongodb"). Fail fast if the container is unreachable.
		registry.add("spring.mongodb.uri",
				() -> "mongodb://localhost:27018/projector_it?serverSelectionTimeoutMS=5000");
		registry.add("spring.docker.compose.enabled", () -> "false");
	}

	@Autowired
	private DiagramService service;

	@Autowired
	private DiagramRepository repository;

	@BeforeEach
	void clean() {
		repository.deleteAll();
	}

	@Test
	void createPersistsWithIdAndTimestamps() {
		DiagramDocument saved = service.create("Order flow", "diagram: Order");

		assertThat(saved.getId()).isNotBlank();
		assertThat(saved.getCreatedAt()).isNotNull();
		assertThat(saved.getUpdatedAt()).isNotNull();
		assertThat(repository.findById(saved.getId())).isPresent();
	}

	@Test
	void storesInvalidYamlAsIs() {
		DiagramDocument saved = service.create("WIP", "this is not: [valid yaml");

		assertThat(service.get(saved.getId()).getYaml()).isEqualTo("this is not: [valid yaml");
	}

	@Test
	void findAllReturnsSavedDiagrams() {
		service.create("A", "y1");
		service.create("B", "y2");

		assertThat(service.findAll()).extracting(DiagramDocument::getName).containsExactlyInAnyOrder("A", "B");
	}

	@Test
	void updateChangesFieldsButKeepsId() {
		DiagramDocument saved = service.create("Old", "old yaml");

		DiagramDocument updated = service.update(saved.getId(), "New", "new yaml");

		assertThat(updated.getId()).isEqualTo(saved.getId());
		assertThat(updated.getName()).isEqualTo("New");
		assertThat(updated.getYaml()).isEqualTo("new yaml");
		// Mongo stores Instant at millisecond precision, so compare createdAt at that resolution.
		assertThat(updated.getCreatedAt().truncatedTo(ChronoUnit.MILLIS))
				.isEqualTo(saved.getCreatedAt().truncatedTo(ChronoUnit.MILLIS));
	}

	@Test
	void deleteRemovesDiagram() {
		DiagramDocument saved = service.create("Temp", "y");

		service.delete(saved.getId());

		assertThat(repository.findById(saved.getId())).isEmpty();
	}

	@Test
	void getUpdateDeleteThrowForUnknownId() {
		assertThatExceptionOfType(DiagramNotFoundException.class).isThrownBy(() -> service.get("nope"));
		assertThatExceptionOfType(DiagramNotFoundException.class).isThrownBy(() -> service.update("nope", "n", "y"));
		assertThatExceptionOfType(DiagramNotFoundException.class).isThrownBy(() -> service.delete("nope"));
	}
}
