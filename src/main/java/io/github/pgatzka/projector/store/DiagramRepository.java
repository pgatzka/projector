package io.github.pgatzka.projector.store;

import org.springframework.data.mongodb.repository.MongoRepository;

/** Spring Data repository for saved diagrams. */
public interface DiagramRepository extends MongoRepository<DiagramDocument, String> {
}
