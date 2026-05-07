package io.github.pgatzka.projector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Locale;

@SpringBootApplication
public class ProjectorApplication {

    public static void main(String[] args) {
        // v1.1 B8: pin Hibernate Validator + JVM-default formatting to English
        // regardless of host locale (avoids leaking German validation messages).
        Locale.setDefault(Locale.ENGLISH);
        SpringApplication.run(ProjectorApplication.class, args);
    }
}
