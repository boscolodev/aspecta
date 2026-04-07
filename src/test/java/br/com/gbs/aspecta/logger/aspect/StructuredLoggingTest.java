package br.com.gbs.aspecta.logger.aspect;

import br.com.gbs.aspecta.logger.configurations.LoggerProperties;
import br.com.gbs.aspecta.logger.interfaces.AsyncLogger;
import br.com.gbs.aspecta.logger.masking.MaskingStrategy;
import br.com.gbs.aspecta.logger.providers.DelegatingMessageProvider;
import br.com.gbs.aspecta.logger.utils.SensitiveDataMasker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoggerAspect - saída estruturada JSON")
class StructuredLoggingTest {

    @Mock private AsyncLogger asyncLogger;
    @Mock private DelegatingMessageProvider messageProvider;

    private LoggerProperties props;
    private SensitiveDataMasker masker;
    private LoggerAspect aspect;

    @BeforeEach
    void setUp() {
        props = new LoggerProperties();
        props.setSensitiveKeys(List.of("password", "token"));
        props.setStructuredOutput(true);
        props.setProjectName("TEST");
        masker = new SensitiveDataMasker(props, (List<MaskingStrategy>) null);
        aspect = new LoggerAspect(props, asyncLogger, messageProvider, masker);
    }

    @Test
    @DisplayName("Quando evento de entrada deve conter os campos esperados no JSON")
    void whenEntryEventShouldContainExpectedFieldsInJson() throws Exception {
        String json = invokeJsonBuilder("entry", "proj", "Svc", "doThing", "arg1", null, -1, null);
        assertThat(json).startsWith("{");
        assertThat(json).endsWith("}");
        assertThat(json).contains("\"event\":\"entry\"");
        assertThat(json).contains("\"project\":\"proj\"");
        assertThat(json).contains("\"class\":\"Svc\"");
        assertThat(json).contains("\"method\":\"doThing\"");
        assertThat(json).contains("\"args\":\"arg1\"");
        assertThat(json).doesNotContain("\"durationMs\"");
    }

    @Test
    @DisplayName("Quando evento de saída deve conter durationMs no JSON")
    void whenExitEventShouldContainDurationMsInJson() throws Exception {
        String json = invokeJsonBuilder("exit", "p", "C", "m", "result-val", null, 42L, null);
        assertThat(json).contains("\"event\":\"exit\"");
        assertThat(json).contains("\"result\":\"result-val\"");
        assertThat(json).contains("\"durationMs\":42");
    }

    @Test
    @DisplayName("Quando evento de erro deve conter exception e message no JSON")
    void whenErrorEventShouldContainExceptionAndMessageInJson() throws Exception {
        String json = invokeJsonBuilder("error", "p", "C", "m", "IllegalArgumentException", "bad input", 10L, null);
        assertThat(json).contains("\"event\":\"error\"");
        assertThat(json).contains("\"exception\":\"IllegalArgumentException\"");
        assertThat(json).contains("\"message\":\"bad input\"");
        assertThat(json).contains("\"durationMs\":10");
    }

    @Test
    @DisplayName("Quando traceId presente deve incluir campo traceId no JSON")
    void whenTraceIdPresentShouldIncludeTraceIdFieldInJson() throws Exception {
        String json = invokeJsonBuilder("entry", "p", "C", "m", "args", null, -1, "trace-99");
        assertThat(json).contains("\"traceId\":\"trace-99\"");
    }

    @Test
    @DisplayName("Quando traceId ausente não deve incluir campo traceId no JSON")
    void whenTraceIdAbsentShouldNotIncludeTraceIdFieldInJson() throws Exception {
        String json = invokeJsonBuilder("entry", "p", "C", "m", "args", null, -1, null);
        assertThat(json).doesNotContain("traceId");
    }

    @Test
    @DisplayName("Quando args contêm aspas deve escapar corretamente no JSON")
    void whenArgsContainQuotesShouldEscapeCorrectlyInJson() throws Exception {
        String json = invokeJsonBuilder("entry", "p", "C", "m", "val\"with\"quotes", null, -1, null);
        assertThat(json).contains("\\\"with\\\"");
    }

    @Test
    @DisplayName("Quando args contêm barras invertidas deve escapar corretamente no JSON")
    void whenArgsContainBackslashesShouldEscapeCorrectlyInJson() throws Exception {
        String json = invokeJsonBuilder("entry", "p", "C", "m", "path\\to\\file", null, -1, null);
        assertThat(json).contains("path\\\\to\\\\file");
    }

    private String invokeJsonBuilder(String event, String project, String cls, String method,
                                     String f1, String f2, long dur, String traceId) throws Exception {
        var m = LoggerAspect.class.getDeclaredMethod(
                "buildStructuredJson",
                String.class, String.class, String.class, String.class,
                String.class, String.class, long.class, String.class);
        m.setAccessible(true);
        return (String) m.invoke(aspect, event, project, cls, method, f1, f2, dur, traceId);
    }
}
