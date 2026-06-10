package io.github.pgatzka.projector.store;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/** Enables auditing so {@code @CreatedDate}/{@code @LastModifiedDate} are populated. */
@Configuration
@EnableMongoAuditing
public class MongoConfig {
}
