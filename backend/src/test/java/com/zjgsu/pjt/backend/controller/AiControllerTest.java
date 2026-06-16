package com.zjgsu.pjt.backend.controller;

import com.zjgsu.pjt.backend.common.Result;
import com.zjgsu.pjt.backend.service.AiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiControllerTest {

    @Mock
    private AiService aiService;

    private AiController controller;

    @BeforeEach
    void setUp() {
        controller = new AiController();
        ReflectionTestUtils.setField(controller, "aiService", aiService);
    }

    @Test
    void testAi_ReturnsSuggestionString() {
        when(aiService.generateActivitySuggestion(anyString(), anyString(), anyString())).thenReturn("online");

        Result<String> result = controller.testAi();

        assertThat(result.getCode()).isEqualTo(0);
        assertThat(result.getData()).isEqualTo("online");
    }

    @Test
    void getSuggestion_UsesRequestDefaultsAndReturnsMap() {
        when(aiService.generateActivitySuggestion("music", "", "weekend"))
                .thenReturn("go to a live house");

        Result result = controller.getSuggestion(Map.of("tags", "music", "query", "weekend"));

        assertThat(result.getCode()).isEqualTo(0);
        assertThat(((Map<?, ?>) result.getData()).get("suggestion")).isEqualTo("go to a live house");
    }
}
