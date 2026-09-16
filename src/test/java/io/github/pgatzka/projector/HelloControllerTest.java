package io.github.pgatzka.projector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@WebMvcTest(HelloController.class)
class HelloControllerTest {

    @Autowired
    MockMvcTester mockMvcTester;

    @MockitoBean
    HelloService helloService;

    @Test
    void hello() {
        given(helloService.hello()).willReturn("greeting from service");

        assertThat(mockMvcTester.get().uri("/hello")).hasStatusOk().hasBodyTextEqualTo("greeting from service");
    }
}
