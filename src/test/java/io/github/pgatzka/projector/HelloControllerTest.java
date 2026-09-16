package io.github.pgatzka.projector;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@WebMvcTest(HelloController.class)
@Import(HelloService.class)
class HelloControllerTest {

    @Autowired
    MockMvcTester mockMvcTester;

    @Test
    void hello() {
        assertThat(mockMvcTester.get().uri("/hello")).hasStatusOk().hasBodyTextEqualTo("Hello, World!");
    }
}
