package br.com.gbs.aspecta.logger.aspect;

import br.com.gbs.aspecta.logger.anotations.LogLevel;
import br.com.gbs.aspecta.logger.configurations.LoggerProperties;
import br.com.gbs.aspecta.logger.interfaces.AsyncLogger;
import br.com.gbs.aspecta.logger.providers.DelegatingMessageProvider;
import br.com.gbs.aspecta.logger.utils.SensitiveDataMasker;
import br.com.gbs.aspecta.util.MemoryAppender;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoggerAspect - logStackTrace")
class LoggerAspectStackTraceTest {

    @Mock private LoggerProperties loggerProperties;
    @Mock private AsyncLogger asyncLogger;
    @Mock private DelegatingMessageProvider messageProvider;
    @Mock private SensitiveDataMasker masker;

    private LoggerAspect aspect;
    private MemoryAppender appender;
    private Logger aspectLogger;

    @BeforeEach
    void setUp() {
        aspect = new LoggerAspect(loggerProperties, asyncLogger, messageProvider, masker);
        aspectLogger = (Logger) LoggerFactory.getLogger(LoggerAspect.class);
        aspectLogger.setLevel(Level.ALL);
        appender = new MemoryAppender();
        appender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        appender.start();
        aspectLogger.addAppender(appender);
    }

    @AfterEach
    void tearDown() {
        appender.detachFrom(aspectLogger);
    }

    @Test
    @DisplayName("Quando nível DEBUG deve logar stack trace em nível DEBUG")
    void whenDebugLevelShouldLogStackTraceAtDebugLevel() {
        aspect.logStackTrace(LogLevel.DEBUG, new RuntimeException("debug-ex"));
        assertThat(appender.contains("Stack trace:", Level.DEBUG)).isTrue();
        assertThat(appender.contains("Stack trace:", Level.INFO)).isFalse();
        assertThat(appender.contains("Stack trace:", Level.ERROR)).isFalse();
    }

    @Test
    @DisplayName("Quando nível INFO deve logar stack trace em nível INFO e não em ERROR")
    void whenInfoLevelShouldLogStackTraceAtInfoLevelNotError() {
        aspect.logStackTrace(LogLevel.INFO, new RuntimeException("info-ex"));
        assertThat(appender.contains("Stack trace:", Level.INFO)).isTrue();
        assertThat(appender.contains("Stack trace:", Level.ERROR)).isFalse();
    }

    @Test
    @DisplayName("Quando nível WARN deve logar stack trace em nível WARN")
    void whenWarnLevelShouldLogStackTraceAtWarnLevel() {
        aspect.logStackTrace(LogLevel.WARN, new RuntimeException("warn-ex"));
        assertThat(appender.contains("Stack trace:", Level.WARN)).isTrue();
        assertThat(appender.contains("Stack trace:", Level.ERROR)).isFalse();
    }
}
