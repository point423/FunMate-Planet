package com.zjgsu.pjt.backend.config;

import com.zjgsu.pjt.backend.common.Result;
import jakarta.validation.Valid;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleValidationExceptions_ReturnsFieldErrorMessage() throws Exception {
        BindingResult bindingResult = new BeanPropertyBindingResult(new SampleRequest(), "sampleRequest");
        bindingResult.addError(new FieldError("sampleRequest", "name", "name is required"));
        Method method = GlobalExceptionHandlerTest.class.getDeclaredMethod("sampleEndpoint", SampleRequest.class);
        MethodParameter parameter = new MethodParameter(method, 0);

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(parameter, bindingResult);

        Result<Map<String, String>> result = handler.handleValidationExceptions(exception);

        assertThat(result.getCode()).isEqualTo(400);
        assertThat(result.getMessage()).contains("name", "name is required");
    }

    @Test
    void handleBindException_ReturnsFieldErrorMessage() {
        BindException exception = new BindException(new SampleRequest(), "sampleRequest");
        exception.addError(new FieldError("sampleRequest", "age", "age is invalid"));

        Result<Map<String, String>> result = handler.handleBindException(exception);

        assertThat(result.getCode()).isEqualTo(400);
        assertThat(result.getMessage()).contains("age", "age is invalid");
    }

    @Test
    void handleIllegalArgumentException_ReturnsBadRequest() {
        Result<String> result = handler.handleIllegalArgumentException(new IllegalArgumentException("bad input"));

        assertThat(result.getCode()).isEqualTo(400);
        assertThat(result.getMessage()).isEqualTo("bad input");
    }

    @Test
    void handleRuntimeException_ReturnsInternalServerError() {
        Result<String> result = withMutedErr(() -> handler.handleRuntimeException(new RuntimeException("boom")));

        assertThat(result.getCode()).isEqualTo(500);
        assertThat(result.getMessage()).contains("boom");
    }

    @Test
    void handleException_ReturnsUnknownError() {
        Result<String> result = withMutedErr(() -> handler.handleException(new Exception("unknown")));

        assertThat(result.getCode()).isEqualTo(500);
        assertThat(result.getMessage()).contains("unknown");
    }

    private <T> T withMutedErr(Supplier<T> action) {
        PrintStream original = System.err;
        ByteArrayOutputStream ignored = new ByteArrayOutputStream();
        PrintStream muted = new PrintStream(ignored);
        try {
            System.setErr(muted);
            return action.get();
        } finally {
            System.setErr(original);
            muted.close();
        }
    }

    @SuppressWarnings("unused")
    private void sampleEndpoint(@Valid SampleRequest request) {
    }

    private static class SampleRequest {
        private String name;
    }
}
