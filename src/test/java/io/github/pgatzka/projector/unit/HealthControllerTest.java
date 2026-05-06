package io.github.pgatzka.projector.unit;

import io.github.pgatzka.projector.rest.controller.HealthController;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class HealthControllerTest {

    @Test
    void healthReturnsStatusOk() {
        HealthController controller = new HealthController();

        Map<String, String> body = controller.health();

        assertThat(body).containsEntry("status", "ok");
    }
}
