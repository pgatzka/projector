package io.github.pgatzka.projector.config;

import io.github.pgatzka.projector.audit.AuditRecordListener;
import org.jooq.impl.DefaultRecordListenerProvider;
import org.springframework.boot.jooq.autoconfigure.DefaultConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JooqConfig {

    @Bean
    public DefaultConfigurationCustomizer jooqConfigurationCustomizer(AuditRecordListener listener) {
        return config -> config.set(new DefaultRecordListenerProvider(listener));
    }
}
