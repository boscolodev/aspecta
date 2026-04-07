package br.com.gbs.aspecta.logger.aspect;

import br.com.gbs.aspecta.logger.anotations.LogLevel;
import br.com.gbs.aspecta.logger.anotations.LogOn;
import br.com.gbs.aspecta.logger.anotations.LogSkip;
import br.com.gbs.aspecta.logger.configurations.LoggerProperties;
import br.com.gbs.aspecta.logger.service.AsyncLoggerService;
import br.com.gbs.aspecta.util.MemoryAppender;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@DisplayName("LoggerAspect - integração")
class LoggerAspectIntegrationTest {

    @Autowired private TestService testService;
    @Autowired private LoggerProperties loggerProperties;

    private MemoryAppender appender;
    private Logger asyncLogger;
    private Logger loggerAspectLogger;

    @BeforeEach
    void attachAppender() throws InterruptedException {
        asyncLogger = (Logger) LoggerFactory.getLogger(AsyncLoggerService.class);
        asyncLogger.setLevel(Level.ALL);

        loggerAspectLogger = (Logger) LoggerFactory.getLogger(LoggerAspect.class);
        loggerAspectLogger.setLevel(Level.ALL);

        appender = new MemoryAppender();
        appender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        appender.start();
        asyncLogger.addAppender(appender);
        loggerAspectLogger.addAppender(appender);

        loggerProperties.setEnabled(true);
        loggerProperties.setStructuredOutput(false);
        MDC.clear();
    }

    @AfterEach
    void detachAppender() {
        appender.detachFrom(asyncLogger);
        appender.detachFrom(loggerAspectLogger);
        loggerAspectLogger.setLevel(null);
        MDC.clear();
    }

    @Test
    @DisplayName("Quando dados sensíveis presentes nos args deve mascarar na entrada")
    void whenSensitiveDataInArgsShouldMaskInEntryLog() throws InterruptedException {
        testService.sensitiveMethod("{\"password\":\"secret123\",\"name\":\"João\"}");
        waitForAsync();
        assertThat(appender.containsMessage("secret123")).isFalse();
        assertThat(appender.containsMessage("João")).isTrue();
    }

    @Test
    @DisplayName("Quando sensitiveData é false deve logar todos os args em texto claro")
    void whenSensitiveDataFalseShouldLogAllArgsInPlainText() throws InterruptedException {
        testService.noMaskingMethod("myPassword123");
        waitForAsync();
        assertThat(appender.containsMessage("myPassword123")).isTrue();
    }

    @Test
    @DisplayName("Quando campo excluído do mask deve aparecer visível no log")
    void whenFieldExcludedFromMaskShouldBeVisibleInLog() throws InterruptedException {
        testService.excludeMaskMethod("{\"email\":\"user@test.com\",\"password\":\"pw\"}");
        waitForAsync();
        assertThat(appender.containsMessage("user@test.com")).isTrue();
        assertThat(appender.containsMessage("pw")).isFalse();
    }

    @Test
    @DisplayName("Quando exceção com dados sensíveis na mensagem deve mascarar no log de erro")
    void whenExceptionWithSensitiveMessageShouldMaskInErrorLog() throws InterruptedException {
        assertThatThrownBy(() -> testService.throwWithSensitiveMessage()).isInstanceOf(RuntimeException.class);
        waitForAsync();
        assertThat(appender.containsMessage("hunter2")).isFalse();
        assertThat(appender.containsMessage("***")).isTrue();
    }

    @Test
    @DisplayName("Quando nível DEBUG configurado deve logar em nível DEBUG")
    void whenDebugLevelConfiguredShouldLogAtDebugLevel() throws InterruptedException {
        testService.debugMethod("val");
        waitForAsync();
        assertThat(appender.contains("val", Level.DEBUG)).isTrue();
    }

    @Test
    @DisplayName("Quando nível WARN configurado deve logar em nível WARN")
    void whenWarnLevelConfiguredShouldLogAtWarnLevel() throws InterruptedException {
        testService.warnMethod("val");
        waitForAsync();
        assertThat(appender.contains("val", Level.WARN)).isTrue();
    }

    @Test
    @DisplayName("Quando structured output habilitado deve emitir log em formato JSON")
    void whenStructuredOutputEnabledShouldEmitJsonLog() throws InterruptedException {
        loggerProperties.setStructuredOutput(true);
        testService.basicMethod("hello");
        waitForAsync();
        assertThat(appender.getEvents()).anyMatch(e -> e.getFormattedMessage().startsWith("{\"event\":"));
        loggerProperties.setStructuredOutput(false);
    }

    @Test
    @DisplayName("Quando structured output habilitado e traceId presente deve incluir traceId no JSON")
    void whenStructuredOutputEnabledAndTraceIdPresentShouldIncludeTraceIdInJson() throws InterruptedException {
        loggerProperties.setStructuredOutput(true);
        MDC.put("traceId", "struct-trace-999");
        testService.basicMethod("x");
        waitForAsync();
        assertThat(appender.containsMessage("struct-trace-999")).isTrue();
        loggerProperties.setStructuredOutput(false);
    }

    @Test
    @DisplayName("Quando traceId presente deve incluir no log em texto plano")
    void whenTraceIdPresentShouldIncludeInPlainLog() throws InterruptedException {
        MDC.put("traceId", "req-trace-42");
        testService.basicMethod("x");
        waitForAsync();
        assertThat(appender.containsMessage("req-trace-42")).isTrue();
    }

    @Test
    @DisplayName("Quando logger desabilitado não deve emitir nenhum log")
    void whenLoggerDisabledShouldEmitNoLogs() throws InterruptedException {
        loggerProperties.setEnabled(false);
        testService.basicMethod("x");
        waitForAsync();
        assertThat(appender.getEvents()).isEmpty();
        loggerProperties.setEnabled(true);
    }

    @Test
    @DisplayName("Quando nível de log inativo deve ignorar o método sem logar")
    void whenLogLevelInactiveShouldSkipMethodWithoutLogging() throws InterruptedException {
        loggerAspectLogger.setLevel(Level.ERROR);
        testService.basicMethod("x");
        waitForAsync();
        assertThat(appender.getEvents()).isEmpty();
        loggerAspectLogger.setLevel(Level.ALL);
    }

    @Test
    @DisplayName("Quando método anotado com LogSkip não deve ser logado")
    void whenMethodAnnotatedWithLogSkipShouldNotBeLogged() throws InterruptedException {
        testService.skippedMethod();
        waitForAsync();
        assertThat(appender.containsMessage("skipped")).isFalse();
    }

    @Test
    @DisplayName("Quando logStackTrace ativado deve registrar stack trace ao lançar exceção")
    void whenLogStackTraceEnabledShouldLogStackTrace() throws InterruptedException {
        assertThatThrownBy(() -> testService.throwWithStackTrace()).isInstanceOf(RuntimeException.class);
        waitForAsync();
        assertThat(appender.contains("Stack trace:", Level.INFO)).isTrue();
    }

    @Test
    @DisplayName("Quando structured output e nível DEBUG deve emitir JSON via dispatchRaw em DEBUG")
    void whenStructuredOutputAndDebugLevelShouldDispatchRawDebug() throws InterruptedException {
        loggerProperties.setStructuredOutput(true);
        testService.debugMethod("val");
        waitForAsync();
        assertThat(appender.getEvents()).anyMatch(e ->
                e.getFormattedMessage().startsWith("{\"event\":") && e.getLevel() == Level.DEBUG);
        loggerProperties.setStructuredOutput(false);
    }

    @Test
    @DisplayName("Quando structured output e nível WARN deve emitir JSON via dispatchRaw em WARN")
    void whenStructuredOutputAndWarnLevelShouldDispatchRawWarn() throws InterruptedException {
        loggerProperties.setStructuredOutput(true);
        testService.warnMethod("val");
        waitForAsync();
        assertThat(appender.getEvents()).anyMatch(e ->
                e.getFormattedMessage().startsWith("{\"event\":") && e.getLevel() == Level.WARN);
        loggerProperties.setStructuredOutput(false);
    }

    private void waitForAsync() throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(300);
    }

    @TestConfiguration
    static class Cfg {
        @Bean TestService testService() { return new TestService(); }
    }

    @LogOn
    static class TestService {

        @LogOn(sensitiveData = true)
        public String sensitiveMethod(String json) { return json; }

        @LogOn(sensitiveData = false)
        public String noMaskingMethod(String raw) { return raw; }

        @LogOn(excludeFromMask = {"email"})
        public String excludeMaskMethod(String json) { return json; }

        @LogOn(sensitiveData = true)
        public void throwWithSensitiveMessage() {
            throw new RuntimeException("Login failed: password=hunter2 is wrong");
        }

        @LogOn(level = LogLevel.DEBUG)
        public String debugMethod(String v) { return v; }

        @LogOn(level = LogLevel.WARN)
        public String warnMethod(String v) { return v; }

        public String basicMethod(String v) { return v; }

        @LogSkip
        public void skippedMethod() {}

        @LogOn(logStackTrace = true)
        public void throwWithStackTrace() {
            throw new RuntimeException("test-stack-trace");
        }
    }
}
