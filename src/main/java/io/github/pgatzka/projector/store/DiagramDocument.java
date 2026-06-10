package io.github.pgatzka.projector.store;

import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * A saved diagram: a free-form name and the raw YAML source. The YAML is the single source
 * of truth; the SVG and parsed model are derived on demand and not stored. Timestamps are
 * maintained by Mongo auditing (see {@link MongoConfig}).
 */
@Document(collection = "diagrams")
public class DiagramDocument {

	@Id
	private String id;
	private String name;
	private String yaml;
	@CreatedDate
	private Instant createdAt;
	@LastModifiedDate
	private Instant updatedAt;

	protected DiagramDocument() {
	}

	public DiagramDocument(String name, String yaml) {
		this.name = name;
		this.yaml = yaml;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getYaml() {
		return yaml;
	}

	public void setYaml(String yaml) {
		this.yaml = yaml;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
