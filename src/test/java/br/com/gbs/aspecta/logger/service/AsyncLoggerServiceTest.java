package br.com.gbs.aspecta.logger.service;

import br.com.gbs.aspecta.util.MemoryAppender;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AsyncLoggerService")
class AsyncLoggerServiceTest {

    private AsyncLoggerService service;
    private MemoryAppender appender;
    private Logger logger;

    @BeforeEach
    void setUp() {
        service = new AsyncLoggerService();
        logger = (Logger) LoggerFactory.getLogger(AsyncLoggerService.class);
        logger.setLevel(Level.ALL);
        appender = new MemoryAppender();
        appender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void tearDown() {
        appender.detachFrom(logger);
        logger.setLevel(null);
    }

    @Test
    @DisplayName("Quando logDebug chamado deve registrar mensagem em nível DEBUG")
    void whenLogDebugCalledShouldLogAtDebugLevel() {
        service.logDebug("mensagem debug {}", "arg");
        assertThat(appender.contains("mensagem debug arg", Level.DEBUG)).isTrue();
    }

    @Test
    @DisplayName("Quando logInfo chamado deve registrar mensagem em nível INFO")
    void whenLogInfoCalledShouldLogAtInfoLevel() {
        service.logInfo("mensagem info {}", "arg");
        assertThat(appender.contains("mensagem info arg", Level.INFO)).isTrue();
    }

    @Test
    @DisplayName("Quando logWarn chamado deve registrar mensagem em nível WARN")
    void whenLogWarnCalledShouldLogAtWarnLevel() {
        service.logWarn("mensagem warn {}", "arg");
        assertThat(appender.contains("mensagem warn arg", Level.WARN)).isTrue();
    }

    @Test
    @DisplayName("Quando logError chamado deve registrar mensagem em nível ERROR")
    void whenLogErrorCalledShouldLogAtErrorLevel() {
        service.logError("mensagem error {}", "arg");
        assertThat(appender.contains("mensagem error arg", Level.ERROR)).isTrue();
    }
}
