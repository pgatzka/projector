package io.github.pgatzka.projector;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class HelloServiceTest {

    HelloService helloService = new HelloService();

    @Test
    void hello() {
        assertThat(helloService.hello()).isEqualTo("Hello, World!");
    }
}
