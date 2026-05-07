package io.github.pgatzka.projector.unit;

import io.github.pgatzka.projector.jooq.enums.LabelColor;
import io.github.pgatzka.projector.rest.exception.RestExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RestExceptionHandlerTest {

    private RestExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new RestExceptionHandler();
    }

    @Test
    void handleHttpMessageNotReadable_withoutCause_returnsStatus400() {
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
        when(ex.getCause()).thenReturn(null);

        ProblemDetail problem = handler.handleUnreadable(ex);

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(problem.getDetail()).contains("could not be read");
    }

    @Test
    void handleMethodArgumentTypeMismatch_withEnum_returns400AndErrors() {
        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
            "purple", LabelColor.class, "color", null, null
        );

        ProblemDetail problem = handler.handleTypeMismatch(ex);

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) problem.getProperties().get("errors");
        assertThat(errors).isNotNull();
        assertThat(errors).containsKey("color");
        assertThat(errors.get("color")).contains("Must be one of:");
        assertThat(errors.get("color")).contains("gray");
    }

    @Test
    void handleMethodArgumentTypeMismatch_withNonEnum_returns400AndGenericMessage() {
        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
            "notanumber", Integer.class, "page", null, null
        );

        ProblemDetail problem = handler.handleTypeMismatch(ex);

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) problem.getProperties().get("errors");
        assertThat(errors).isNotNull();
        assertThat(errors).containsKey("page");
        assertThat(errors.get("page")).contains("Invalid value");
    }
}
